package wee.digital.library.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import wee.digital.library.util.Logger

open class UsbReceiver(private val vendorIdList: IntArray) : BroadcastReceiver() {

    private val log: Logger = Logger(this::class.java)

    fun findDevice() {
        val map = mutableMapOf<Int, UsbEvent>()
        for (vendorId in vendorIdList) {
            val usb = Usb.getDevice(vendorId)
            map[vendorId] = when {
                null == usb -> {
                    UsbEvent(Usb.DETACHED, usb)
                }
                Usb.hasPermission(usb) -> {
                    UsbEvent(Usb.GRANTED, usb)
                }
                else -> {
                    Usb.requestPermission(usb)
                    UsbEvent(Usb.ATTACHED, usb)
                }
            }
        }
        UsbLiveData.instance.value = map
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val usb = intent?.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as? UsbDevice
        if (null != usb) {
            onUsbLiveDataUpdate(usb, intent)
        }
    }

    private fun Intent.getPermission(): Boolean {
        return getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
    }

    private fun onUsbLiveDataUpdate(usb: UsbDevice, intent: Intent) {

        val status = when (intent.action) {

            UsbManager.ACTION_USB_DEVICE_DETACHED -> Usb.DETACHED

            UsbManager.ACTION_USB_DEVICE_ATTACHED -> if (Usb.hasPermission(usb)) {
                Usb.GRANTED
            } else {
                Usb.requestPermission(usb)
                Usb.ATTACHED
            }

            Usb.PERMISSION -> if (intent.getPermission()) {
                Usb.GRANTED
            } else {
                Usb.DENIED
            }

            else -> null

        } ?: return

        UsbLiveData.instance.value = mutableMapOf(usb.vendorId to UsbEvent(status, usb))
    }

}