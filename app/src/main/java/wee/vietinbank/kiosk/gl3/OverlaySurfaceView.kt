package wee.vietinbank.kiosk.gl3

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class OverlaySurfaceView(context: Context?) : GLSurfaceView(context) {

    private var render: OverlayRenderer
    private var previousX = 0f
    private var previousY = 0f

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.001f
    }

    init {
        setEGLContextClientVersion(3)
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        render = OverlayRenderer()
        setRenderer(render)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                render.x = render.x - dx * TOUCH_SCALE_FACTOR
                val dy = y - previousY
                render.y = render.y - dy * TOUCH_SCALE_FACTOR
            }
        }
        previousX = x
        previousY = y
        return true
    }


}