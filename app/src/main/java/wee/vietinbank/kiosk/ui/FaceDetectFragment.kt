package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.face_detect.*
import wee.digital.camera.RealSense
import wee.digital.library.usb.Usb
import wee.digital.library.usb.UsbEvent
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class FaceDetectFragment : BaseFragment() {


    override val layoutResourceId: Int = R.layout.face_detect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop, viewClose)
        onUsbEvent(Usb.deviceStatus(RealSense.VENDOR_ID))

        detectorView.observe(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            deviceViewStart -> {
                RealSense.start()
            }
            deviceViewStop -> {
                RealSense.stop()
            }
            viewClose -> {
                popBackStack()
            }
        }
    }

    private fun onUsbEvent(event: UsbEvent?) {
        when (event?.status) {
            Usb.DETACHED -> {
                deviceTextViewMessage.text = "Device had been detached."
            }
            Usb.GRANTED -> {
                deviceTextViewMessage.text = event.usb?.productName
            }
        }
    }

}