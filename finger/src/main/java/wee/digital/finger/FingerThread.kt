package wee.digital.finger

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

/**
 * Subclass [HandlerThread] wait for async return value of [FingerCommand.captureImage]:[FingerData]
 * and callback [FingerThread.listener]:[FingerListener] on ui thread
 */
class FingerThread private constructor() : HandlerThread(NAME) {

    companion object {

        private var fingerPressed: Boolean = false

        private const val NAME: String = "fingerprint"

        var instance: FingerThread? = null
            private set

        fun start() {
            if (instance?.isAlive == true) return
            instance = FingerThread().apply {
                start()
                join(300)
            }
        }

        fun quit() {
            HeroFun.isScanning = false
            instance?.pauseScan()
            instance?.quitSafely()
            instance = null
        }

    }

    var listener: FingerListener? = null

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        scanHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message?) {
                val message = uiHandler.obtainMessage()
                message.obj = msg?.obj
                message.sendToTarget()
            }
        }
    }

    private val uiHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            (msg?.obj as? FingerData)?.also {
                detectFingerprint(it)
            }
        }
    }

    private fun detectFingerprint(data: FingerData) {

        val hasTemplate = data.patternRate > 8

        if (!hasTemplate && fingerPressed) {
            HeroFun.d("Unpressed")
            listener?.onFingerUnpressed()
        }

        if (hasTemplate && !fingerPressed) {
            HeroFun.d("Pressed")
            listener?.onFingerPressed()
        }

        HeroFun.d("Template detected rate ${data.patternRate}")
        listener?.onFingerData(data)
        fingerPressed = hasTemplate
    }


    /**
     * Scan handler
     */
    private var scanHandler: Handler? = null

    private val scanRunnable: Runnable = object : Runnable {
        override fun run() {
            if (this@FingerThread.isAlive) try {
                scanHandler?.obtainMessage()?.apply {
                    if (HeroFun.isScanning) {
                        obj = FingerCommand.captureImage
                        sendToTarget()
                    }
                }
                scanHandler?.postDelayed(this, HeroFun.SCAN_DELAY)
                return
            } catch (ignore: Exception) {
                // current thread dead
            }
        }
    }

    fun startScan(listener: FingerListener) {
        this.listener = listener
        if (HeroFun.isScanning) return
        HeroFun.isScanning = true
        join(500)
        scanHandler?.removeCallbacks(scanRunnable)
        scanHandler?.post(scanRunnable)
        startTimeout()
    }

    fun pauseScan() {
        HeroFun.isScanning = false
        timeoutHandler.removeCallbacks(timeoutRunnable)
        scanHandler?.removeCallbacks(scanRunnable)
    }


    /**
     * Timeout handler
     */
    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        pauseScan()
        HeroFun.d("Scan timeout")
        listener?.onFingerScanTimeout()
    }

    private fun startTimeout() {
        if (HeroFun.hasTimeout) {
            timeoutHandler.postDelayed(timeoutRunnable, HeroFun.timeout)
        }
    }

}