package wee.digital.finger

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * https://www.globalsources.com/gsol/I/Fingerprint-module/p/sm/1172182843.htm#1172182843
 */
object HeroFun {

    /**
     * Application
     */
    private var mApp: Application? = null

    var app: Application
        set(value) {
            mApp = value
        }
        get() {
            if (null == mApp) throw NullPointerException("module not be set")
            return mApp!!
        }

    /**
     * Log
     */
    private const val TAG = "HeroFun"

    fun d(s: Any?) {
        if (BuildConfig.DEBUG) Log.d(TAG, s.toString())
    }

    fun d(e: Throwable) {
        if (BuildConfig.DEBUG) Log.e(TAG, e.message)
    }

    /**
     * Configs
     */
    private const val PERMISSION = ".USB_PERMISSION"

    private const val IMAGE_WIDTH = 256

    private const val IMAGE_HEIGHT = 360

    const val VENDOR_ID: Int = 1155

    const val PRODUCT_ID: Int = 22288

    const val IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT

    const val WSQ_SIZE = 512 * 512

    const val NO_TIMEOUT: Long = -1

    var SCAN_DELAY: Long = 320

    var timeout: Long = -1

    var isScanning = false

    val hasTimeout: Boolean get() = timeout > 0L

    /**
     * @param fingerBytes length must is [.IMAGE_SIZE]
     */
    fun getBitmap(fingerBytes: ByteArray?): Bitmap? {
        if (null == fingerBytes) return null
        val image = IntArray(IMAGE_SIZE)
        for (i in 0 until IMAGE_SIZE) {
            val v: Int = fingerBytes[i].toInt() and 0xff
            image[i] = Color.rgb(v, v, v)
        }
        return Bitmap.createBitmap(image, IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.RGB_565)
    }

    fun getBytes(bitmap: Bitmap): ByteArray? {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    fun getBase64(bitmap: Bitmap): String? {
        val bytes = getBytes(bitmap) ?: return null
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Usb util
     */
    @JvmStatic
    val usbManager: UsbManager
        get() = app.getSystemService(Context.USB_SERVICE) as UsbManager

    @JvmStatic
    val usbDevices: Collection<UsbDevice>
        get() = usbManager.deviceList.values

    private val intentFilter: IntentFilter by lazy {
        IntentFilter(PERMISSION).also {
            it.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            it.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
    }

    @JvmStatic
    val device: UsbDevice?
        get() {
            usbDevices.forEach {
                if (it.vendorId == VENDOR_ID) return it
            }
            return null
        }

    private var usbReceiver: BroadcastReceiver? = null

    private fun usbReceiver(permissionGranted: () -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val usb = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (usb.vendorId != VENDOR_ID) return
                if (intent.action === UsbManager.ACTION_USB_DEVICE_DETACHED) return
                if (intent.action === UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    if (usbManager.hasPermission(usb)) {
                        permissionGranted()
                    } else {
                        requestPermission(usb)
                    }
                }
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    permissionGranted()
                }
            }
        }
    }

    @JvmStatic
    fun open(usb: UsbDevice?): UsbDeviceConnection {
        return usbManager.openDevice(usb)
    }

    @JvmStatic
    fun requestPermission(usb: UsbDevice?, permissionGranted: () -> Unit = {}) {
        if (hasPermission(usb)) {
            permissionGranted()
            return
        }

        if (usbReceiver == null) {
            usbReceiver = usbReceiver(permissionGranted).also {
                app.registerReceiver(it, intentFilter)
            }
        }

        usb ?: return
        val permissionIntent = PendingIntent.getBroadcast(app, 1234, Intent(PERMISSION), 0)
        usbManager.requestPermission(usb, permissionIntent)
    }

    @JvmStatic
    fun requestPermission(permissionGranted: () -> Unit = {}) {
        requestPermission(device, permissionGranted)
    }

    @JvmStatic
    fun hasPermission(usb: UsbDevice?): Boolean {
        usb ?: return false
        return usbManager.hasPermission(usb)
    }

}