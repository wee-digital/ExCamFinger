package wee.vietinbank.kiosk.ui.finger

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.finger.*
import wee.digital.finger.FingerCommand
import wee.digital.finger.FingerData
import wee.digital.finger.FingerListener
import wee.digital.finger.HeroFun
import wee.digital.library.usb.Usb
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment

class FingerFragment : BaseFragment(), FingerListener {

    override val layoutResourceId: Int = R.layout.finger

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewScan, deviceViewStop, deviceViewClear)
    }

    override fun onDestroyView() {
        FingerCommand.close()
        super.onDestroyView()
    }

    override fun onViewClick(view: View) {
        when (view) {
            deviceViewScan -> {
                FingerCommand.open()
                FingerCommand.scan(this)
            }
            deviceViewStop -> {
                FingerCommand.close()
            }
            deviceViewClear -> {
                Usb.forceClose(HeroFun.VENDOR_ID)
            }
        }
    }

    override fun onFingerData(data: FingerData) {
        deviceImageViewFinger.setImageBitmap(data.bitmap)
    }

}