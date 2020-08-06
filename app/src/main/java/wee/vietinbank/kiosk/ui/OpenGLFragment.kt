package wee.vietinbank.kiosk.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import wee.vietinbank.kiosk.R
import wee.vietinbank.kiosk.base.BaseFragment
import wee.vietinbank.kiosk.gl3.OverlaySurfaceView
import wee.vietinbank.kiosk.gl3.hasOpenGLES30

class OpenGLFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.opengl


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasOpenGLES30) {
            (view as ViewGroup).addView(OverlaySurfaceView(context))
        }
    }


}