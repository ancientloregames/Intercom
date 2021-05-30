package com.ancientlore.intercom.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ToolbarManager(private val toolbar: Toolbar) {

	fun setTitle(@StringRes textResId: Int) {
		toolbar.setTitle(textResId)
	}

	fun setTitle(text: CharSequence) {
		toolbar.title = text
	}

	fun setSubtitle(@StringRes textResId: Int) {
		toolbar.setSubtitle(textResId)
	}

	fun setSubtitle(text: CharSequence) {
		toolbar.subtitle = text
	}

	fun setLogo(bitmap: Bitmap) {
		setLogo(BitmapDrawable(toolbar.resources, bitmap))
	}

	fun setLogo(drawable: Drawable) {
		toolbar.logo = drawable
	}

	fun setLogo(url: String, fallback: Drawable? = null) {
		setLogo(Uri.parse(url), fallback)
	}

	fun setLogo(uri: Uri, fallback: Drawable? = null) {

		Glide.with(toolbar.context)
			.load(uri)
			.fallback(fallback)
			.listener(object : RequestListener<Drawable> {
				override fun onResourceReady(resource: Drawable?, model: Any?,
				                             target: Target<Drawable>?, dataSource: DataSource?,
				                             isFirstResource: Boolean): Boolean {

					toolbar.logo = resource

					return true
				}
				override fun onLoadFailed(e: GlideException?, model: Any?,
				                          target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

					runOnUiThread {
						toolbar.logo = fallback
					}

					return true
				}
			})
			.submit()
	}

	fun enableBackButton(listener: View.OnClickListener) {
		toolbar.setNavigationIcon(R.drawable.ic_nav_back)
		toolbar.setNavigationOnClickListener(listener)
	}
}