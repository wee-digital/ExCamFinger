package wee.digital.camera.job

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import wee.digital.camera.RealSense
import wee.digital.camera.detector.FaceDetector

/**
 * simple callback wrapper on UI
 */
class DebugDetectJob(private var uiListener: Listener) :
        FaceDetector.OptionListener,
        FaceDetector.DataListener,
        FaceDetector.StatusListener {

    interface Listener : FaceDetector.OptionListener, FaceDetector.DataListener,
            FaceDetector.StatusListener

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


    /**
     * [FaceDetector.OptionListener] implement
     */
    override fun onMaskLabel(label: String, confidence: Float): Boolean {
        uiCallback { onMaskLabel(label, confidence) }
        return true
    }

    override fun onDepthLabel(label: String, confidence: Float): Boolean {
        uiCallback { onDepthLabel(label, confidence) }
        return true
    }

    override fun onFaceScore(score: Float): Boolean {
        uiCallback { onFaceScore(score) }
        return true
    }

    override fun onFaceRect(left: Int, top: Int, width: Int, height: Int): Boolean {
        uiCallback { onFaceRect(left, top, width, height) }
        return true
    }

    override fun onFaceDegrees(x: Double, y: Double): Boolean {
        uiCallback { onFaceDegrees(x, y) }
        return true
    }


    /**
     * [FaceDetector.DataListener] implement
     */
    override fun onFaceColorImage(bitmap: Bitmap?) {
        uiCallback { onFaceColorImage(bitmap) }
    }

    override fun onFaceDepthImage(bitmap: Bitmap?) {
        uiCallback { onFaceDepthImage(bitmap) }
    }

    override fun onPortraitImage(label: String, cropColor: Bitmap, cropDepth: Bitmap) {
        uiCallback { onPortraitImage(label, cropColor, cropDepth) }
    }


    override fun onFaceLeaved() {
        uiCallback {
            onFaceScore(0f)
            onFaceRect(-1, -1, 0, 0)
            onFaceDegrees(0.0, 0.0)
            onMaskLabel("", 100f)
            onDepthLabel("", 100f)
            onFaceColorImage(null)
            onFaceDepthImage(null)
            onFaceLeaved()
        }
    }

    override fun onFacePerformed() {
        uiCallback { onFacePerformed() }
    }

    override fun onFaceChanged() {
        uiCallback { onFaceChanged() }
    }


}