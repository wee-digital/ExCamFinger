package wee.vietinbank.kiosk.base

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import wee.digital.library.util.Logger


abstract class BaseActivity : AppCompatActivity(), BaseView {

    val log: Logger = Logger(this::class.java)

    protected abstract val layoutResourceId: Int

    /**
     * [BaseView] implement
     */
    override val fragmentContainerId: Int
        get() {
            throw Resources.NotFoundException("BaseView.FragmentContainer() must be implement with resource id return value")
        }

    override val fragmentActivity: FragmentActivity get() = this

    override fun onViewClick(view: View) {
    }


    /**
     * [AppCompatActivity] override
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && null != data) {
            onReceivedDataResult(requestCode, data)
        }
    }

    /**
     * [BaseActivity] utilities
     */
    open fun onReceivedDataResult(requestCode: Int, data: Intent) {
    }


}