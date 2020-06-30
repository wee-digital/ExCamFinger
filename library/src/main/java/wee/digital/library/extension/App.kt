package wee.digital.library.extension

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.*
import androidx.core.content.ContextCompat
import wee.digital.library.Library
import java.io.BufferedReader
import java.io.InputStreamReader

private val app: Application get() = Library.app

val appVersion: String
    get() {
        return try {
            app.packageManager.getPackageInfo(app.packageName, 0)
                    .versionName
        } catch (e: PackageManager.NameNotFoundException) {
            return "v1.0"
        }
    }

val packageName: String get() = app.applicationContext.packageName

fun restartApp() {
    val intent = app.packageManager.getLaunchIntentForPackage(packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    app.startActivity(intent)
}

fun navigate(packageName: String) {
    val intent = app.packageManager.getLaunchIntentForPackage(packageName) ?: return
    app.startActivity(intent)
}

fun anim(@AnimRes res: Int): Animation {
    return AnimationUtils.loadAnimation(app, res)
}

fun drawable(@DrawableRes res: Int): Drawable {
    return ContextCompat.getDrawable(app, res)!!
}

fun color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(app, res)
}

fun string(@StringRes res: Int): String {
    return app.getString(res)
}

fun string(@StringRes res: Int, vararg args: Any?): String {
    return try {
        String.format(app.getString(res), *args)
    } catch (e: Exception) {
        e.message ?: ""
    }
}

fun pixel(@DimenRes res: Int): Float {
    return app.resources.getDimension(res)
}

fun readAsset(filename: String): String {
    val sb = StringBuilder()
    BufferedReader(InputStreamReader(app.assets.open(filename))).useLines { lines ->
        lines.forEach {
            sb.append(it)
        }
    }
    return sb.toString()
}