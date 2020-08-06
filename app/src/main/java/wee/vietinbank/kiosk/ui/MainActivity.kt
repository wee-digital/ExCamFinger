package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.main.*
import wee.digital.camera.RealSense
import wee.digital.finger.HeroFun
import wee.digital.library.usb.Usb
import wee.digital.library.usb.UsbEvent
import wee.digital.library.usb.UsbLiveData
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseActivity
import wee.vietinbank.kiosk.io.Cashino
import wee.vietinbank.kiosk.ui.camera.CamFaceFragment
import wee.vietinbank.kiosk.ui.camera.CameraFragment
import wee.vietinbank.kiosk.ui.camera.FaceFragment
import wee.vietinbank.kiosk.ui.finger.FingerFragment
import wee.vietinbank.kiosk.ui.printer.PrinterFragment


class MainActivity : BaseActivity() {

    override val layoutResourceId: Int = R.layout.main

    override val fragmentContainerId: Int = R.id.viewContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addClickListener(viewCamera, viewFaceDetect, viewCameraFaceDetect,
                viewPrinter, viewFingerprint, viewDevices)

        UsbLiveData.builder(this)
                .devices(RealSense.VENDOR_ID, Cashino.VENDOR_ID, HeroFun.VENDOR_ID)
                .observe {
                    onUsbReceiver(it)
                }
        add(OpenGLFragment(), true)
    }

    override fun onViewClick(view: View) {
        when (view) {
            viewCamera -> add(CameraFragment(), true)
            viewOpenGL -> add(OpenGLFragment(), true)
            viewFaceDetect -> add(FaceFragment(), true)
            viewCameraFaceDetect -> add(CamFaceFragment(), true)
            viewPrinter -> add(PrinterFragment(), true)
            viewFingerprint -> add(FingerFragment(), true)
        }
    }

    private fun onUsbReceiver(map: Map<Int, UsbEvent>) {
        viewCamera.onBindUsbState(RealSense.VENDOR_ID, "Camera", map)
        viewPrinter.onBindUsbState(Cashino.VENDOR_ID, "Printer", map)
        viewFingerprint.onBindUsbState(HeroFun.VENDOR_ID, "Finger", map)
        var s = "Plugged devices:\n"
        Usb.deviceList.forEach {
            s += "${it.productName} - ${it.manufacturerName}\n"
        }
        viewDevices.text = s
    }

    private fun TextView.onBindUsbState(vendorId: Int, name: String, map: Map<Int, UsbEvent>) {
        val state = map[vendorId] ?: return
        text = "$name usb " + when (state.status) {
            Usb.ATTACHED -> "had been attached: ${state.usb?.productName}"
            Usb.GRANTED -> "had been granted: ${state.usb?.productName}"
            else -> "not found"
        }
    }

}