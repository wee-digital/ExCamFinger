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

    private val colorizer: Colorizer = Colorizer().apply {
        setValue(Option.COLOR_SCHEME, 0f)
    }
    private val align: Align = Align(StreamType.COLOR)

    private var pipeline: Pipeline? = null
    private var pipelineProfile: PipelineProfile? = null

    private var colorBitmap: Bitmap? = null
    private var depthBitmap: Bitmap? = null

    private var isStreaming = false

    private var streamHandler: Handler? = null
    private var streamThread: HandlerThread? = null
    val streamProcessor = StreamProcessor()

    init {
        streamThread = HandlerThread("streaming").also {
            it.start()
            streamHandler = Handler(it.looper)
        }
    }

    fun onStart() {
        if (isStreaming) return
        try {
            pipeline = Pipeline().also {
                val config = Config().apply {
                    enableStream(StreamType.COLOR, 0, COLOR_WIDTH, COLOR_HEIGHT,
                            StreamFormat.RGB8, FRAME_RATE)
                    enableStream(StreamType.DEPTH, 0, DEPTH_WIDTH, DEPTH_HEIGHT,
                            StreamFormat.Z16, FRAME_RATE)
                }
                pipelineProfile = it.start(config)?.apply {
                    isStreaming = true
                    streamHandler?.post(streamProcessor)
                }
            }
        } catch (t: Throwable) {
            isStreaming = false
        }
    }

    fun onStop() {
        isStreaming = false
        try {
            streamThread?.quitSafely()
            streamProcessor.isWaitForFrame = false
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

    private fun hardwareReset() {
        try {
            pipelineProfile?.device?.hardwareReset()
        } catch (ignore: RuntimeException) {
        }
    }

    inner class StreamProcessor : Runnable {

        private var mFrameCount = FRAME_MAX_COUNT

        var isWaitForFrame: Boolean = false

        override fun run() {
            if (isWaitForFrame) {
                repeat()
                return
            }
            if (!RealSense.imagesLiveData.hasObservers() && !RealSense.depthLiveData.hasObservers()) {
                repeat()
                return
            }
            isWaitForFrame = true
            try {
                FrameReleaser().use { fr ->
                    mFrameCount--
                    val frameSet: FrameSet = pipeline!!.waitForFrames(TIME_WAIT).releaseWith(fr)
                    when {
                        mFrameCount > 0 -> {
                            fr.frameRelease(frameSet)
                        }
                        mFrameCount < FRAME_MAX_SLEEP -> {
                            mFrameCount = FRAME_MAX_COUNT
                        }
                    }
                    repeat()
                }
            } catch (e: Throwable) {
                e(e.message)
                repeat()
            }

        }

        private fun repeat() {
            isWaitForFrame = false
            if (isStreaming) {
                streamHandler?.postDelayed(this, 80)
            }
        }

        fun aware() {
            mFrameCount = FRAME_MAX_COUNT
        }
    }

}