package wee.digital.finger

/**
 * The fingerprint scan data detector callback: link [FingerThread.listener] getter/setter
 * see [FingerThread.detectFingerprint]
 * View / androidx.lifecycle.LifecycleOwner / android.viewmodel.LifecycleOwner
 * Callback on main thread
 */
interface FingerListener {

    /**
     * Callback after implementation class on [FingerCommand.scan]
     */
    fun onStartScan() {}

    /**
     * Callback after implementation class on [FingerCommand.pause]
     */
    fun onPauseScan() {}

    /**
     * @see [FingerData]
     */
    fun onFingerData(data: FingerData)

    /**
     * Callback only once until finger pressed on usb fingerprint scanner module
     */
    fun onFingerUnpressed() {}

    /**
     * Callback only once until finger unpressed on usb fingerprint scanner module
     */
    fun onFingerPressed() {}

    /**
     * Modify link [HeroFun.timeout] static long value default is -1(no timeout)
     */
    fun onFingerScanTimeout() {}

}