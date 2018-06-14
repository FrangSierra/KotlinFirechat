package frangsierra.kotlinfirechat.util

import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


fun ImageView.setCircularImage(url: String?) {
    val requestOptions = RequestOptions
            .circleCropTransform()
            .override(width, height)

    Glide.with(context)
            .asDrawable()
            .apply(requestOptions)
            .into(this)
            .waitForLayout()
            .clearOnDetach()
}

fun ImageView.setCircularImage(@DrawableRes drawableRes: Int) {
    val requestOptions = RequestOptions
            .circleCropTransform()
            .override(width, height)

    Glide.with(context)
            .asDrawable()
            .load(drawableRes)
            .apply(requestOptions)
            .into(this)
            .waitForLayout()
            .clearOnDetach()
}

fun ImageView.setThumnailImage(url: String) {
    val requestOptions = RequestOptions
            .centerCropTransform()
            .override(width, height)

    Glide.with(context)
            .asDrawable()
            .load(url)
            .apply(requestOptions)
            .into(this)
            .waitForLayout()
            .clearOnDetach()
}

fun ImageView.setImage(url: String?, onLoadError: () -> Unit = {}) {
    val requestOptions = RequestOptions
            .centerCropTransform()
            .override(width, height)

    Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    onLoadError()
                    return true
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean = false
            })
            .thumbnail(0.1f)
            .into(this)
            .waitForLayout()
            .clearOnDetach()
}