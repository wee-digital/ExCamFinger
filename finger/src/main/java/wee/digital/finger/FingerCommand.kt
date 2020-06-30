package wee.digital.finger

import com.HEROFUN.LAPI

//import com.machinezoo.sourceafis.FingerprintCompatibility
object FingerCommand {

    private var lapi: LAPI? = null

    private var deviceId: Int = 0

    private var currentImage = ByteArray(HeroFun.IMAGE_SIZE)

    private val patternRate: Int
        get() = if (deviceId == 0) 0 else lapi?.GetImageQuality(deviceId, currentImage) ?: 0

    //
    val captureImage: FingerData?
        get() {
            if (deviceId == 0) return null
            currentImage = ByteArray(HeroFun.IMAGE_SIZE)
            lapi?.GetImage(deviceId, currentImage)
            return FingerData(currentImage, patternRate)
        }

    private val ansiTemplate = ByteArray(LAPI.FPINFO_STD_MAX_SIZE)

    private val isoTemplate = ByteArray(LAPI.FPINFO_STD_MAX_SIZE)

    val ansiScore: Int get() = lapi?.CompareTemplates(deviceId, ansiTemplate, isoTemplate) ?: 0

    val isoScore get() = lapi?.CompareTemplates(deviceId, ansiTemplate, isoTemplate)

    val make: String? get() = lapi?.GetMake(deviceId)

    val model: String? get() = lapi?.GetModel(deviceId)

    val serial: String? get() = lapi?.GetSerialNumber(deviceId)

    val nfiQuality: Int get() = lapi?.GetNFIQuality(deviceId, currentImage) ?: 0

    private var isCommand = false

    private fun command(block: () -> Unit) {
        if (isCommand) return
        isCommand = true
        synchronized(deviceId) {
            Thread {
                try {
                    block()
                    isCommand = false
                } catch (e: UnsatisfiedLinkError) {
                } catch (e: IllegalArgumentException) {
                }
            }.start()
        }
    }

    fun open() {
        command {
            if (null == lapi) lapi = LAPI()
            deviceId = lapi?.OpenDevice(1, 2) ?: 0
        }
        FingerThread.start()
    }

    fun close() {
        FingerThread.instance?.listener?.onPauseScan()
        FingerThread.quit()
        command {
            if (null == lapi) lapi = LAPI()
            if (deviceId == 0) deviceId = lapi?.OpenDevice(1, 2) ?: 0
            lapi?.CloseDevice(deviceId)
            deviceId = 0
        }

    }

    fun scan(listener: FingerListener) {
        FingerThread.instance?.apply {
            startScan(listener)
            listener.onStartScan()
        }
    }

    fun pause() {
        FingerThread.instance?.apply {
            listener?.onPauseScan()
            pauseScan()
        }
    }

    fun forceClose() {

    }

    /**
     * matches two templates and returns similar match score is for 1:1 Matching and only used in fingerprint verification.
     * must be normalized by method [createTemplate]
     * @return similar match score(0~100) of two fingerprint templates.
     */
    fun compare(fingerprintTemplate1: ByteArray, fingerprintTemplate2: ByteArray): Int {
        if (deviceId == 0) throw NullPointerException("Device not opened")
        return lapi?.CompareTemplates(deviceId, fingerprintTemplate1, fingerprintTemplate2) ?: 0
    }

    fun createTemplate(rawFingerprint: ByteArray): ByteArray {
        val template = ByteArray(1024)
        lapi?.CreateANSITemplate(deviceId, rawFingerprint, template)
        return template
    }

    fun getWSQImage(rawFingerprint: ByteArray): ByteArray {
        val wsqImage = ByteArray(HeroFun.WSQ_SIZE)
        lapi?.CompressToWSQImage(deviceId, rawFingerprint, wsqImage)
        return wsqImage
    }

}