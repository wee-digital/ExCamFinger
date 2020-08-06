package wee.digital.camera.dev

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import wee.digital.camera.RealSense
import wee.digital.camera.detector.FaceDetector

/**
 * simple callback wrapper on UI
 */
class DepthDetectJob(private var uiListener: Listener) :
        FaceDetector.OptionListener,
        FaceDetector.DataListener,
        FaceDetector.StatusListener {


    private val imagesObserver = Observer<Pair<Bitmap, Bitmap>?> {
        it?.apply {
            detector.detectFace(first, second)
        }
    }

    private val detector: FaceDetector = FaceDetector().also {
        it.optionListener = this
        it.dataListener = this
        it.statusListener = this
    }

    fun observe(lifecycleOwner: LifecycleOwner) {
        RealSense.imagesLiveData.observe(lifecycleOwner, imagesObserver)
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






    override fun onFaceScore(score: Float): Boolean {
        return score > 0.9
    }

    override fun onFaceRect(left: Int, top: Int, width: Int, height: Int): Boolean {
        return width > 90
    }

    override fun onFaceDegrees(x: Double, y: Double): Boolean {
        return x in -45f..45f && y in -45f..45f
    }

    override fun onPortraitImage(bitmap: Bitmap) {
    }

    override fun onFacePerformed() {
    }

    override fun onFaceLeaved() {
    }

    interface Listener {
        fun onDepthData()
    }

}