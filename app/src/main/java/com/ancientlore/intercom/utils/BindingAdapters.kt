package com.ancientlore.intercom.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.bumptech.glide.Glide


@BindingAdapter("android:src")
fun setImageResource(imageView: AppCompatImageView, uri: Uri) {
	if (uri.isNotEmpty()) {
		Glide.with(imageView.context)
			.load(uri)
			.into(imageView)
	} else {
		imageView.setImageURI(null)
	}
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, uri: Uri) {
	if (uri.isNotEmpty()) {
		Glide.with(imageView.context)
			.load(uri)
			.into(imageView)
	} else {
		imageView.setImageURI(null)
	}
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, data: Any) {
	when (data) {
		is Uri -> Glide.with(imageView.context)
			.load(data)
			.into(imageView)
		is String -> Glide.with(imageView.context)
			.load(Uri.parse(data))
			.into(imageView)
		is Drawable -> imageView.setImageDrawable(data)
		is Int -> imageView.setImageResource(data)
		else -> imageView.setImageURI(null)
	}
}

@BindingAdapter("srcCompat")
fun setImageResource(imageView: AppCompatImageView, res: Int) {
	imageView.setImageResource(res)
}

@BindingAdapter("srcCompat")
fun setImageResource(imageView: ImageView, res: Int) {
	imageView.setImageResource(res)
}

@BindingAdapter("android:visibility")
fun setViewVisibility(view: View, visible: Boolean) {
	view.visibility = if (visible) View.VISIBLE else View.GONE
}