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

        const val VIDEO_WIDTH = 1280
        const val VIDEO_HEIGHT = 720
        const val VIDEO_SIZE = VIDEO_WIDTH * VIDEO_HEIGHT * 3

        const val COLOR_WIDTH = 640
        const val COLOR_HEIGHT = 480
        const val COLOR_SIZE = COLOR_WIDTH * COLOR_HEIGHT * 3

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

    private var videoBitmap: Bitmap? = null
    private var colorBitmap: Bitmap? = null

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
                            fr.releaseDepthData(frameSet)
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
                debug("streaming, error: " + e.message)
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
                enableStream(StreamType.COLOR, 0, VIDEO_WIDTH, VIDEO_HEIGHT, StreamFormat.RGB8, FRAME_RATE)
                enableStream(StreamType.DEPTH, 0, COLOR_WIDTH, COLOR_HEIGHT, StreamFormat.Z16, FRAME_RATE)
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
        mHandlerThread?.quitSafely()
        pipelineProfile?.close()
        pipeline?.stop()
    }

    private fun FrameReleaser.frameRelease(frameSet: FrameSet) {

        val videoFrame: Frame = frameSet.first(StreamType.COLOR).releaseWith(this)

        val colorFrame: Frame = align.process(frameSet)
                .releaseWith(this)
                .applyFilter(colorizer)
                .releaseWith(this)
                .first(StreamType.DEPTH)
                .releaseWith(this)

        imageRelease(videoFrame, colorFrame)
    }

    private fun imageRelease(videoFrame: Frame, colorFrame: Frame) {
        ByteArray(VIDEO_SIZE).also {
            videoFrame.getData(it)
            videoBitmap = it.rgbToBitmap(VIDEO_WIDTH, VIDEO_HEIGHT)
        }
        ByteArray(VIDEO_SIZE).also {
            colorFrame.getData(it)
            colorBitmap = it.rgbToBitmap(VIDEO_WIDTH, VIDEO_HEIGHT)
        }
        if (videoBitmap != null && colorBitmap != null) {
            RealSense.imagesLiveData.postValue(Pair(videoBitmap!!, colorBitmap!!))
        }
    }

    private fun FrameReleaser.releaseDepthData(frameSet: FrameSet) {
        val videoFrame: Frame = frameSet.first(StreamType.COLOR).releaseWith(this)

        val alignFrame: FrameSet = align.process(frameSet)
                .releaseWith(this)

        val colorFrame: Frame = alignFrame
                .applyFilter(colorizer)
                .releaseWith(this)
                .first(StreamType.DEPTH)
                .releaseWith(this)

        val depthFrame: DepthFrame = alignFrame
                .first(StreamType.DEPTH)
                .releaseWith(this)
                .`as`(Extension.DEPTH_FRAME)

        val w = depthFrame.width
        val h = depthFrame.height
        val dis = depthFrame.getDistance(w / 2, h / 2)
        debug("w:$w, h:$h, dis:$dis")

        imageRelease(videoFrame, colorFrame)
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