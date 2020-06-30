package wee.vietinbank.kiosk.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import wee.digital.library.extension.hideKeyboard
import wee.digital.library.util.Logger

abstract class BaseFragment : Fragment(), LifecycleOwner, BaseView {

    private lateinit var lifecycleRegistry: LifecycleRegistry

    val log: Logger = Logger(this::class.java)

    abstract val layoutResourceId: Int


    /**
     * [BaseView] implement
     */
    override val fragmentActivity: FragmentActivity? get() = activity

    override val fragmentContainerId: Int get() = baseActivity?.fragmentContainerId!!

    override fun onViewClick(view: View) {
    }

    /**
     * [Fragment] override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutResourceId, container, false)
        view.setOnTouchListener { _, _ -> true }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && null != data) {
            onReceivedDataResult(requestCode, data)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        view.post { hideKeyboard() }
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDestroyView() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroyView()
    }

    /**
     * [BaseFragment] utilities
     */
    protected val baseActivity: BaseActivity? get() = activity as? BaseActivity

    open fun onReceivedDataResult(code: Int, intent: Intent) {
    }

}