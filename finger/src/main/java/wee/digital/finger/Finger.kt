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
object Finger {

    const val VENDOR_ID: Int = 1155

    const val PRODUCT_ID: Int = 22288

    private const val IMAGE_WIDTH = 256

    private const val IMAGE_HEIGHT = 360

    const val IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT

    const val WSQ_SIZE = 512 * 512

    private const val TAG = "Finger"

    private const val PERMISSION = ".USB_PERMISSION"

    val hasTimeout: Boolean get() = timeout > 0L

    private var mApp: Application? = null

    var app: Application
        set(value) {
            mApp = value
        }
        get() {
            if (null == mApp) throw NullPointerException("module not be set")
            return mApp!!
        }

    var timeout: Long = -1

    const val NO_TIMEOUT: Long = -1

    var SCAN_DELAY: Long = 320

    var isScanning = false

    fun d(s: Any?, vararg arg: Any?) {
        if (BuildConfig.DEBUG) Log.d(TAG, if (s == null) "null" else String.format(s.toString(), *arg))
    }

    fun d(e: Throwable) {
        if (BuildConfig.DEBUG) Log.e(TAG, e.message)
    }

    @JvmStatic
    fun device(): UsbDevice? {
        for (usb in usbDevices()) {
            if (usb.vendorId == VENDOR_ID && usb.productId == PRODUCT_ID) return usb
        }
        return null
    }

    fun usbManager(): UsbManager {
        return app.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    fun usbDevices(): Collection<UsbDevice> {
        return usbManager().deviceList.values
    }

    fun usbReceiver(permissionGranted: () -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val usb = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (usb.vendorId != VENDOR_ID || usb.productId != PRODUCT_ID) return
                if (intent.action === UsbManager.ACTION_USB_DEVICE_DETACHED) return
                if (intent.action === UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    if (usbManager().hasPermission(usb)) {
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

    fun intentFilter(): IntentFilter {
        val intFilter = IntentFilter(PERMISSION)
        intFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        return intFilter
    }

    @JvmStatic
    fun open(usb: UsbDevice?): UsbDeviceConnection {
        return usbManager().openDevice(usb)
    }

    fun requestPermission(usb: UsbDevice?) {
        d("on request permission")
        val permissionIntent = PendingIntent.getBroadcast(app, 1234, Intent(PERMISSION), 0)
        usbManager().requestPermission(usb, permissionIntent)
    }

    @JvmStatic
    fun hasPermission(usb: UsbDevice?): Boolean {
        return usbManager().hasPermission(usb)
    }

    fun authorize(application: Application, permissionGranted: () -> Unit) {
        app = application
        application.registerReceiver(usbReceiver(permissionGranted), intentFilter())
        val usb = device() ?: return
        if (hasPermission(usb)) {
            d("device permission had been granted")
            permissionGranted()
        } else {
            d("device request permission")
            requestPermission(usb)
        }
    }

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

}