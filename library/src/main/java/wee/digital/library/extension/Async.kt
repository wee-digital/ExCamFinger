package wee.digital.library.extension


import android.os.Handler
import android.os.Looper

val uiHandler: Handler get() = Handler(Looper.getMainLooper())

val isOnMainThread: Boolean get() = Looper.myLooper() == Looper.getMainLooper()

fun mainThread(block: () -> Unit) {
    if (isOnMainThread) block()
    else uiHandler.post { block() }
}

fun mainThread(delay: Long, block: () -> Unit) {
    uiHandler.postDelayed({ block() }, delay)
}

fun post(block: () -> Unit) {
    Handler().post { block() }
}

fun post(delay: Long, block: () -> Unit) {
    Handler().postDelayed({ block() }, delay)
}