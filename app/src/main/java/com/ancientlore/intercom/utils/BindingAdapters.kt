package com.ancientlore.intercom.utils

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