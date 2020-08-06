package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.camera_face.*
import wee.digital.camera.RealSense
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class CamFaceFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.camera_face

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)

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
        }
    }

}