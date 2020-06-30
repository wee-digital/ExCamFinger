package wee.digital.library.view

import android.app.Activity
import android.view.View
import wee.digital.library.extension.hideKeyboard

class ViewClickListener(val block: (View?) -> Unit) : View.OnClickListener {

    private val now: Long get() = System.currentTimeMillis()

    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        if (now - lastClickTime > 300) {
            (v?.context as? Activity)?.hideKeyboard()
            block(v)
        }
        lastClickTime = now
    }
}