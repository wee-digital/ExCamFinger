package wee.digital.library.view

import android.view.View

class ViewTapListener(private val tapCount: Int, val block: (View?) -> Unit) : View.OnClickListener {

    private var lastFastClickTime: Long = 0

    private var clickCount: Int = 0

    private fun resetFastClickCount() {
        lastFastClickTime = 0
        clickCount = 0
    }

    override fun onClick(v: View?) {
        if (System.currentTimeMillis() - lastFastClickTime > 300 || clickCount >= tapCount) {
            clickCount = 0
        }
        lastFastClickTime = System.currentTimeMillis()
        clickCount++
        if (clickCount == tapCount) {
            resetFastClickCount()
            block(v)
        }
    }
}
