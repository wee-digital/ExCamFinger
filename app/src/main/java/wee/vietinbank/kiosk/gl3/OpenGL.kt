package wee.vietinbank.kiosk.gl3

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.renderscript.Int3
import wee.vietinbank.kiosk.App

val hasOpenGLES30: Boolean
    get() {
        val am = App.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

val GL_RED = floatArrayOf(
        Color.red(Color.RED) / 255f,
        Color.green(Color.RED) / 255f,
        Color.blue(Color.RED) / 255f,
        1.0f
)

val GL_GREEN = floatArrayOf(
        Color.red(Color.GREEN) / 255f,
        Color.green(Color.GREEN) / 255f,
        Color.blue(Color.GREEN) / 255f,
        1.0f
)

val GL_BLUE = floatArrayOf(
        Color.red(Color.BLUE) / 255f,
        Color.green(Color.BLUE) / 255f,
        Color.blue(Color.BLUE) / 255f,
        1.0f
)

val GL_YELLOW = floatArrayOf(
        Color.red(Color.YELLOW) / 255f,
        Color.green(Color.YELLOW) / 255f,
        Color.blue(Color.YELLOW) / 255f,
        1.0f
)

val GL_CYAN = floatArrayOf(
        Color.red(Color.CYAN) / 255f,
        Color.green(Color.CYAN) / 255f,
        Color.blue(Color.CYAN) / 255f,
        1.0f
)

val GL_GRAY = floatArrayOf(
        Color.red(Color.GRAY) / 255f,
        Color.green(Color.GRAY) / 255f,
        Color.blue(Color.GRAY) / 255f,
        1.0f
)

val pyramidPoints: FloatArray
    get() {
        val arr = mutableListOf<Float>()
        for (x in 0..9) {
            for (y in 0..9) {
                val z = (x + y) / 2
                arr.add(x.toFloat())
                arr.add(y.toFloat())
                arr.add(z.toFloat())
            }
        }
        return arr.toFloatArray()
    }