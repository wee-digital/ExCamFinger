package wee.vietinbank.kiosk.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.face2.*
import wee.digital.camera.RealSense
import wee.digital.camera.job.DebugDetectJob
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class Face2Fragment : BaseFragment(), DebugDetectJob.UiListener {

    private val adapter = Face2Adapter()

    override val layoutResourceId: Int = R.layout.face2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewStart, deviceViewStop)

        cameraView.observe(this)

        DebugDetectJob(this).observe(viewLifecycleOwner)

        adapter.bind(recyclerViewPortrait) {
            orientation = LinearLayoutManager.HORIZONTAL
            stackFromEnd = true
            reverseLayout = true
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


    override fun onPortraitImage(bitmap: Bitmap) {
        adapter.add(bitmap)
        if (adapter.size > 20) {
            adapter.currentList.removeAt(0)
        }
        recyclerViewPortrait?.smoothScrollToPosition(adapter.lastPosition)
    }

    override fun onFacePerformed() {
    }

    override fun onFaceLeaved() {
    }

}