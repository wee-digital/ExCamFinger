package wee.vietinbank.kiosk.ui.camera

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.camera_depth_detect.*
import wee.digital.camera.RealSense
import wee.digital.camera.d
import wee.digital.camera.fixToRsColorSize
import wee.digital.camera.job.DepthDetectJob
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class DepthDetectFragment : BaseFragment(), DepthDetectJob.Listener {

    override val layoutResourceId: Int = R.layout.camera_depth_detect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)
        imageViewColor.fixToRsColorSize()

        RealSense.depthLiveData.observe(viewLifecycleOwner, Observer {
            imageViewColor.setImageBitmap(it?.first)
        })

        RealSense.coordLiveData.observe(viewLifecycleOwner, Observer {
            d("Coords: ${it.size}")
            /*if (it.size == 9) {
                textView1.text = String.format("%2f", it[0])
                textView2.text = String.format("%2f", it[1])
                textView3.text = String.format("%2f", it[2])
                textView4.text = String.format("%2f", it[3])
                textView5.text = String.format("%2f", it[4])
                textView6.text = String.format("%2f", it[5])
                textView7.text = String.format("%2f", it[6])
                textView8.text = String.format("%2f", it[7])
                textView9.text = String.format("%2f", it[8])
            }*/
        })

        DepthDetectJob(this).observe(viewLifecycleOwner)

        /* val s = readAsset("face.txt")
         val bmp = s.base64ToBitmap()*/
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