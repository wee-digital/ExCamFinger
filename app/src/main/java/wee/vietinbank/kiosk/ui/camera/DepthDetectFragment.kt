package wee.vietinbank.kiosk.ui.camera

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.camera_depth_detect.*
import wee.digital.camera.RealSense
import wee.digital.camera.job.DepthDetectJob
import wee.digital.camera.parse
import wee.digital.camera.readAsset
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.gl3.OverlaySurfaceView
import wee.vietinbank.kiosk.gl3.hasOpenGLES30

class DepthDetectFragment : BaseFragment(), DepthDetectJob.Listener {

    override val layoutResourceId: Int = R.layout.camera_depth_detect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)
        if (hasOpenGLES30) {
            (view as ViewGroup).addView(OverlaySurfaceView(context!!), 0)
        }

        /*RealSense.depthLiveData.observe(viewLifecycleOwner, Observer {
            imageViewColor.setImageBitmap(it?.first)
        })
        DepthDetectJob(this).observe(viewLifecycleOwner)

        val sV = readAsset("vertices.json")
                ?.parse(Array<Float>::class.java)
                ?.toFloatArray() ?: return


        val sTC = readAsset("texture_coord.json")
                ?.parse(Array<Float>::class.java)
                ?.toFloatArray() ?: return


        println("")*/

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

    /**
     * [DepthDetectJob.Listener] implement
     */
    override fun onDepthData() {
    }

}