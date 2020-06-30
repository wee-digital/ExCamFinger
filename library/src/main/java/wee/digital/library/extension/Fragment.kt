package wee.digital.library.extension

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import wee.digital.library.R

val HORIZONTAL_ANIMATIONS = intArrayOf(
        R.anim.horizontal_enter,
        R.anim.horizontal_exit,
        R.anim.horizontal_pop_enter,
        R.anim.horizontal_pop_exit
)

val VERTICAL_ANIMATIONS = intArrayOf(
        R.anim.vertical_enter,
        R.anim.vertical_exit,
        R.anim.vertical_pop_enter,
        R.anim.vertical_pop_exit
)

val ALPHA_ANIMATIONS = intArrayOf(
        R.anim.fade_in,
        R.anim.fade_out,
        R.anim.fade_in,
        R.anim.fade_out
)

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun FragmentActivity?.addFragment(
        fragment: Fragment, @IdRes container: Int,
        backStack: Boolean = true,
        animations: IntArray? = null
) {
    this ?: return
    val tag = fragment::class.java.simpleName
    supportFragmentManager.scheduleTransaction({
        add(container, fragment, tag)
        if (backStack) addToBackStack(tag)
    }, animations)
}

fun FragmentActivity?.replaceFragment(
        fragment: Fragment, @IdRes container: Int,
        backStack: Boolean = true,
        animations: IntArray? = null
) {
    this ?: return
    val tag = fragment::class.java.simpleName
    supportFragmentManager.scheduleTransaction({
        replace(container, fragment, tag)
        if (backStack) addToBackStack(tag)
    }, animations)
}

fun FragmentActivity?.isExist(cls: Class<*>): Boolean {
    this ?: return false
    val tag = cls.simpleName
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    return null != fragment
}

fun FragmentActivity?.isNotExist(cls: Class<*>): Boolean {
    this ?: return false
    val tag = cls.simpleName
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    return null == fragment
}

fun FragmentActivity?.remove(cls: Class<*>, animations: IntArray? = null) {
    remove(cls.simpleName, animations)
}

fun FragmentActivity?.remove(tag: String?, animations: IntArray? = null) {
    this ?: return
    tag ?: return
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: return
    supportFragmentManager.scheduleTransaction({
        remove(fragment)
    }, animations)
}

fun FragmentManager.scheduleTransaction(
        block: FragmentTransaction.() -> Unit,
        animations: IntArray? = null
) {

    val transaction = beginTransaction()
    if (null != animations) transaction.setCustomAnimations(
            animations[0],
            animations[1],
            animations[2],
            animations[3]
    )
    transaction.block()
    transaction.commitAllowingStateLoss()

}

