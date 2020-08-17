package wee.digital.camera

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import com.intel.realsense.librealsense.*


/**
 * Manufacture: Intel(R) RealSense(TM) Depth Camera SR305
 * Product : Intel(R) RealSense(TM) Depth Camera SR305
 * Vendor ID : 32902
 * Product ID: 2888
 */
class RealSenseControl {

    companion object {

        const val COLOR_WIDTH = 640
        const val COLOR_HEIGHT = 480
        const val COLOR_SIZE = COLOR_WIDTH * COLOR_HEIGHT * 3

        const val DEPTH_WIDTH = 640
        const val DEPTH_HEIGHT = 480
        const val DEPTH_SIZE = DEPTH_WIDTH * DEPTH_HEIGHT * 3

        const val TIME_WAIT = 2000
        const val FRAME_RATE = 10
        const val FRAME_MAX_COUNT = 200 // Run 10s
        const val FRAME_MAX_SLEEP = -20 // Sleep 1s

    }

    private val colorizer = Colorizer().apply {
        setValue(Option.COLOR_SCHEME, 0f)
    }
    private var align: Align = Align(StreamType.COLOR)
    private var pipeline: Pipeline? = null
    private var pipelineProfile: PipelineProfile? = null

    private var colorBitmap: Bitmap? = null
    private var depthBitmap: Bitmap? = null

    private var isDestroy = false
    private var isFrameOK = false
    var isPauseCamera = false
    private var isSleep = false
    private var isProcessingFrame = false
    private var isStreaming = false

    private var mFrameCount = FRAME_MAX_COUNT

    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private val streamRunnable: Runnable = object : Runnable {
        override fun run() {
            var isNext = false

            try {
                FrameReleaser().use { fr ->
                    if (!RealSense.imagesLiveData.hasObservers()) {
                        RealSense.imagesLiveData.postValue(null)
                        isSleep = true
                        true
                    }
                    if (isPauseCamera || isProcessingFrame) {
                        isSleep = true
                        true
                    }
                    isProcessingFrame = true
                    if (!isFrameOK) {
                        isFrameOK = true
                        isProcessingFrame = false
                        isSleep = false
                        true
                    }
                    mFrameCount--
                    val frameSet: FrameSet = pipeline!!.waitForFrames(TIME_WAIT).releaseWith(fr)
                    when {
                        mFrameCount > 0 -> {
                            fr.frameRelease(frameSet)
                            //fr.depthRelease(frameSet)
                        }
                        mFrameCount < FRAME_MAX_SLEEP -> {
                            mFrameCount = FRAME_MAX_COUNT
                            isProcessingFrame = false
                        }
                        else -> {
                            isProcessingFrame = false
                        }
                    }
                    isSleep = false
                    isProcessingFrame = false
                    isNext = true
                }
            } catch (e: Throwable) {
                e(e.message)
                isProcessingFrame = false
            }

            if (!isNext) {
                isFrameOK = false
                hardwareReset()
                Handler().postDelayed({ onCreate() }, 3000)
                return
            }

            if (!isSleep) {
                mHandler?.post(this)
            } else {
                mHandler?.postDelayed(this, 80)
            }
        }
    }

    init {
        mHandlerThread = HandlerThread("streaming").also {
            it.start()
            mHandler = Handler(it.looper)
        }
    }

    fun onCreate() {
        if (isStreaming) return
        try {
            val config = Config().apply {
                enableStream(StreamType.COLOR, 0, COLOR_WIDTH, COLOR_HEIGHT,
                        StreamFormat.RGB8, FRAME_RATE)
                enableStream(StreamType.DEPTH, 0, DEPTH_WIDTH, DEPTH_HEIGHT,
                        StreamFormat.Z16, FRAME_RATE)
            }
            pipeline = Pipeline()
            pipelineProfile = pipeline?.start(config)?.apply {
                isStreaming = true
                mHandler?.post(streamRunnable)
            }
        } catch (t: Throwable) {
            isStreaming = false
        }
    }

    fun onPause() {
        isStreaming = false
        isDestroy = true
        try {
            mHandlerThread?.quitSafely()
            pipelineProfile?.close()
            pipeline?.stop()
        } catch (t: Throwable) {
            e(t.localizedMessage)
        }
    }

    private fun FrameReleaser.frameRelease(frameSet: FrameSet) {

        val colorFrame: Frame = frameSet
                .first(StreamType.COLOR)
                .releaseWith(this)

        val depthFrame: Frame = align.process(frameSet)
                .releaseWith(this)
                .applyFilter(colorizer)
                .releaseWith(this)
                .first(StreamType.DEPTH)
                .releaseWith(this)

        ByteArray(COLOR_SIZE).also {
            colorFrame.getData(it)
            colorBitmap = it.rgbToBitmap(COLOR_WIDTH, COLOR_HEIGHT)
        }
        ByteArray(COLOR_SIZE).also {
            depthFrame.getData(it)
            depthBitmap = it.rgbToBitmap(COLOR_WIDTH, COLOR_HEIGHT)
        }

        RealSense.imagesLiveData.postValue(Pair(colorBitmap, depthBitmap))
    }

    private fun FrameReleaser.depthRelease(frameSet: FrameSet) {

        val colorFrame: Frame = frameSet
                .first(StreamType.COLOR)
                .releaseWith(this)

        val depthFrame: DepthFrame = align.process(frameSet)
                .releaseWith(this)
                .first(StreamType.DEPTH)
                .releaseWith(this)
                .`as`(Extension.DEPTH_FRAME)

        ByteArray(COLOR_SIZE).also {
            colorFrame.getData(it)
            colorBitmap = it.rgbToBitmap(COLOR_WIDTH, COLOR_HEIGHT)
        }


        RealSense.depthLiveData.postValue(Pair(colorBitmap, depthFrame))
    }

    fun hasFace() {
        mFrameCount = FRAME_MAX_COUNT
    }

    private fun hardwareReset() {
        try {
            pipelineProfile?.device?.hardwareReset()
        } catch (ignore: RuntimeException) {
        }
    }


}