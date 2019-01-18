package com.ancientlore.intercom.utils

import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("src")
fun setImageResource(imageView: AppCompatImageView, uri: Uri) {
	imageView.setImageURI(uri)
}