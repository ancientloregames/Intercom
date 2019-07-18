package com.ancientlore.intercom.utils

import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("src")
fun setImageResource(imageView: AppCompatImageView, uri: Uri) {
	imageView.setImageURI(uri)
}

@BindingAdapter("srcCompat")
fun setImageResource(imageView: AppCompatImageView, res: Int) {
	imageView.setImageResource(res)
}

@BindingAdapter("srcCompat")
fun setImageResource(imageView: ImageView, res: Int) {
	imageView.setImageResource(res)
}