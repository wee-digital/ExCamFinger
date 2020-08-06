package wee.vietinbank.kiosk.ui.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.printer.*
import wee.digital.library.usb.Usb
import wee.digital.library.usb.UsbEvent
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.io.Cashino

class PrinterFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.printer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickListener(deviceViewConnect, deviceViewPrint, viewClose)
        onUsbEvent(Usb.deviceStatus(Cashino.VENDOR_ID))
    }

    override fun onViewClick(view: View) {
        when (view) {
            deviceViewConnect -> {

            }
            deviceViewClear -> {
                Usb.forceClose(Cashino.VENDOR_ID)
            }
            deviceViewPrint -> {
                Cashino.print(viewPrintContent.getBitmap())
            }
            viewClose -> {
                popBackStack()
            }
        }
    }

    private fun onUsbEvent(event: UsbEvent?) {
        when (event?.status) {
            Usb.GRANTED -> {
                deviceTextViewMessage.text = event.usb?.productName
            }
            Usb.DETACHED -> {
                deviceTextViewMessage.text = "Device had been detached."
            }
        }
    }

    private fun View.getBitmap(width: Int = this.width, height: Int = this.height): Bitmap {
        if (width > 0 && height > 0) {
            measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        }
        this.layout(0, 0, this.measuredWidth, this.measuredHeight)
        val bitmap = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.background?.draw(canvas)
        draw(canvas)
        return bitmap
    }

}