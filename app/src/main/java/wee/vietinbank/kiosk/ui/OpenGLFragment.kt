package wee.vietinbank.kiosk.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import wee.vietinbank.kiosk.App
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.gl3.OverlaySurfaceView

class OpenGLFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.opengl


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (detectOpenGLES30()) {
            ( view as ViewGroup).addView(OverlaySurfaceView(context))
        }
    }

    private fun detectOpenGLES30(): Boolean {
        val am = App.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }
}