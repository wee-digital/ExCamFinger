package wee.digital.library.extension

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

fun View.animRotateAxisY(duration: Long = 1000, repeatCount: Int = ObjectAnimator.INFINITE): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "rotationY", 0.0f, 360f).also {
        it.duration = duration
        it.interpolator = AccelerateDecelerateInterpolator()
        it.repeatCount = repeatCount
    }
}

fun animFadeIn(duration: Long = 500): Animation {
    val anim = AlphaAnimation(0f, 1f)
    anim.duration = duration
    anim.fillAfter = true
    return anim
}

fun animFadeOut(duration: Long = 500): Animation {
    val anim = AlphaAnimation(1f, 0f)
    anim.duration = duration
    anim.fillAfter = true
    return anim
}

fun Animation?.onAnimationStart(onStart: () -> Unit): Animation? {
    this?.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            onStart()
        }
    })
    return this
}

fun Animation?.onAnimationEnd(onEnd: () -> Unit): Animation? {
    this?.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            onEnd()
        }
    })
    return this
}

fun ObjectAnimator?.onAnimatorEnd(onEnd: () -> Unit): ObjectAnimator? {
    this?.addListener(object : SimpleAnimatorListener {
        override fun onAnimationEnd(animator: Animator?) {
            onEnd()
        }
    })
    return this
}

interface SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {
    }

    override fun onAnimationStart(animation: Animation?) {
    }
}

interface SimpleAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animator: Animator?) {
    }

    override fun onAnimationEnd(animator: Animator?) {
    }

    override fun onAnimationCancel(animator: Animator?) {
    }

    override fun onAnimationStart(animator: Animator?) {
    }
}
