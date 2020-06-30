package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.camera_face_detect.*
import wee.digital.camera.RealSense
import wee.digital.library.usb.Usb
import wee.digital.library.usb.UsbEvent
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class CameraFaceDetectFragment : BaseFragment() {


    override val layoutResourceId: Int = R.layout.camera_face_detect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop, viewClose)
        onUsbEvent(Usb.deviceStatus(RealSense.VENDOR_ID))

        cameraView.observe(this)

        detectorView.observe(this)
        detectorView.faceRectListener = { left, top, width, height ->
            cameraView.targetFace(left, top, width, height)
        }

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