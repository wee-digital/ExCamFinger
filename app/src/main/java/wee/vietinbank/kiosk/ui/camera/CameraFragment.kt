package wee.vietinbank.kiosk.ui.camera

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.camera.*
import wee.digital.camera.RealSense
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class CameraFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.camera

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)

        cameraView.observe(this)
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