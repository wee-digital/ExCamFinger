package wee.digital.camera.job

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.intel.realsense.librealsense.DepthFrame
import com.intel.realsense.librealsense.Points
import wee.digital.camera.RealSense
import wee.digital.camera.detector.DepthDetector

/**
 * simple callback wrapper on UI
 */
class DepthDetectJob(private var uiListener: Listener) :
        DepthDetector.OptionListener,
        DepthDetector.DataListener,
        DepthDetector.StatusListener {

    private val imagesObserver = Observer<Pair<Bitmap?, Points?>?> {
        it?.apply {
            detector.detectFace(it.first, it.second)
        }
    }

    private val detector: DepthDetector = DepthDetector().also {
        it.optionListener = this
        it.dataListener = this
        it.statusListener = this
    }

    fun observe(lifecycleOwner: LifecycleOwner) {
        RealSense.depthLiveData.observe(lifecycleOwner, imagesObserver)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun destroy() {
                detector.destroy()
            }
        })
    }

    private fun uiCallback(block: Listener.() -> Unit) {
        Handler(Looper.getMainLooper()).post {
            uiListener.block()
        }
    }

    /**
     * [DepthDetector.OptionListener] implement
     */
    override fun onFaceScore(score: Float): Boolean {
        return score > 0
    }

    override fun onFaceRect(left: Int, top: Int, width: Int, height: Int): Boolean {
        return width > 0
    }

    /**
     * [DepthDetector.DataListener] implement
     */
    override fun onPortraitImage(bitmap: Bitmap) {
    }

    /**
     * [DepthDetector.StatusListener] implement
     */
    override fun onFacePerformed() {
        RealSense.hasFace()
    }

    override fun onFaceLeaved() {
    }

    interface Listener {
        fun onDepthData()
    }

}