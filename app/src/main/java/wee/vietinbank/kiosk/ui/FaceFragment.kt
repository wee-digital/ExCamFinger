package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.face.*
import wee.digital.camera.RealSense
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class FaceFragment : BaseFragment() {


    override val layoutResourceId: Int = R.layout.face

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)

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
        }
    }

}