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

        const val COLOR_WIDTH = 1280
        const val COLOR_HEIGHT = 720
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

            val isNext = try {
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
                                framesProcessing(frameSet, fr)
                                //depthProcessing(frameSet, fr)
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
                    true
                }
            } catch (e: Exception) {
                debug("streaming, error: " + e.message)
                isProcessingFrame = false
                false
            }

            if (isNext) {
                if (!isSleep) {
                    mHandler?.post(this)
                } else {
                    mHandler?.postDelayed(this, 80)
                }
            } else {
                isFrameOK = false
                hardwareReset()
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
                enableStream(StreamType.COLOR, 0, COLOR_WIDTH, COLOR_HEIGHT, StreamFormat.RGB8, FRAME_RATE)
                enableStream(StreamType.DEPTH, 0, DEPTH_WIDTH, DEPTH_HEIGHT, StreamFormat.Z16, FRAME_RATE)
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

    private fun framesProcessing(frameSet: FrameSet, fr: FrameReleaser) {
        val colorFrame: Frame = frameSet.first(StreamType.COLOR).releaseWith(fr)
        val depthFrame: Frame = align.process(frameSet)
                .releaseWith(fr)
                .applyFilter(colorizer)
                .releaseWith(fr)
                .first(StreamType.DEPTH)
                .releaseWith(fr)
        ByteArray(COLOR_SIZE).also {
            colorFrame.getData(it)
            colorBitmap = it.rgbToBitmap(COLOR_WIDTH, COLOR_HEIGHT)
        }
        ByteArray(COLOR_SIZE).also {
            depthFrame.getData(it)
            depthBitmap = it.rgbToBitmap(COLOR_WIDTH, COLOR_HEIGHT)
        }
        if (colorBitmap != null && depthBitmap != null) {
            RealSense.imagesLiveData.postValue(Pair(colorBitmap!!, depthBitmap!!))
        }
    }

    private fun depthProcessing(frameSet: FrameSet, fr: FrameReleaser) {
        val depthFrame: Frame = align.process(frameSet)
                .releaseWith(fr)
                .applyFilter(colorizer)
                .releaseWith(fr)
                .first(StreamType.DEPTH)
                .releaseWith(fr)
        val d = depthFrame.`as`<DepthFrame>(Extension.DEPTH_FRAME)
        debug(d.getDistance(COLOR_WIDTH / 2, COLOR_HEIGHT / 2))
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