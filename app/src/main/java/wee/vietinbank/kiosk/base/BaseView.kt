package wee.vietinbank.kiosk.base

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import wee.digital.library.extension.VERTICAL_ANIMATIONS
import wee.digital.library.extension.addFragment
import wee.digital.library.extension.remove
import wee.digital.library.extension.replaceFragment
import wee.digital.library.view.ViewClickListener


interface BaseView : LifecycleOwner {

    val fragmentActivity: FragmentActivity?

    fun onViewClick(view: android.view.View)

    fun addClickListener(vararg views: android.view.View) {
        for (v in views) v.setOnClickListener(ViewClickListener {
            onViewClick(v)
        })
    }



    /**
     * Fragment utilities
     */
    val fragmentContainerId: Int

    fun add(fragment: Fragment, stack: Boolean = false, animations: IntArray? = VERTICAL_ANIMATIONS) {
        fragmentContainerId.also {
            fragmentActivity?.addFragment(fragment, it, stack, animations)
        }
    }

    fun replace(fragment: Fragment, stack: Boolean = false, animations: IntArray? = VERTICAL_ANIMATIONS) {
        fragmentContainerId.also {
            fragmentActivity?.replaceFragment(fragment, it, stack)
        }
    }


    /**
     * Navigation utilities
     */
    fun getIntent(cls: Class<*>): Intent {
        return Intent(fragmentActivity, cls)
    }

    fun start(cls: Class<*>) {
        fragmentActivity?.startActivity(Intent(fragmentActivity, cls))
    }

    fun startFinish(cls: Class<*>) {
        fragmentActivity?.apply {
            startActivity(Intent(fragmentActivity, cls))
            finish()
        }
    }

    fun startClear(cls: Class<*>) {
        fragmentActivity?.apply {
            val intent = Intent(this, cls)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.startActivity(intent)
            this.finish()
        }

    }

    fun moveTaskToBack() {
        fragmentActivity?.moveTaskToBack(true)
    }

    fun popBackStack(cls: Class<*>) {
        fragmentActivity?.remove(cls)
    }

    fun popBackStack() {
        fragmentActivity?.supportFragmentManager?.popBackStack()
    }

    fun <T> LiveData<T?>.observe(block: (T?) -> Unit) {
        observe(this@BaseView, Observer { block(it) })
    }

    fun <T> LiveData<T?>.nonNull(block: (T) -> Unit) {
        observe(this@BaseView, Observer { if (null != it) block(it) })
    }

    fun <T> LiveData<T>.stop() {
        removeObservers(this@BaseView)
    }

}