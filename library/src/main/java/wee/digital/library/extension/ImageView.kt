package wee.digital.library.extension

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyGlideApp : AppGlideModule()

fun ImageView.load(url: String?, @DrawableRes placeHolder: Int = 0) {
    GlideApp.with(context)
            .load(url)
            .placeholder(placeHolder)
            .error(placeHolder)
            .into(this)
}

fun ImageView.load(bitmap: Bitmap?, width: Int, height: Int) {
    GlideApp.with(context)
            .load(bitmap)
            .override(width, height)
            .into(this)
}

fun ImageView.load(@DrawableRes res: Int) {
    GlideApp.with(context).load(res).into(this)
}

fun ImageView.load(bytes: ByteArray?) {
    GlideApp.with(context).load(bytes).into(this)
}

fun ImageView.tint(@ColorRes res: Int) {
    this.post { this.setColorFilter(ContextCompat.getColor(context, res)) }
}

