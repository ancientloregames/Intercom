package com.ancientlore.intercom.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.ancientlore.intercom.view.ChatImageView
import com.ancientlore.intercom.view.DrawableCompatTextView
import com.ancientlore.intercom.view.TextDrawable
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
fun setImageResource(imageView: ChatImageView, uri: Uri) {
	imageView.setImageURI(uri)
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

@BindingAdapter(value = [
	"android:src",
	"asCircle",
	"abbrText", "abbrSize", "abbrTextColor", "abbrBackColor",
	"fallback", "placeholder", "error",
	"fallbackTint", "placeholderTint", "errorTint"], requireAll = false)
fun setImageResource(imageView: ImageView, data: Any,
                     asCircle: Boolean = false,
                     abbrText: String? = null, abbrSize: Float = 0f, abbrTextColor: Int = Color.WHITE, abbrBackColor: Int = Color.TRANSPARENT,
                     fallback: Drawable?, placeholder: Drawable?, error: Drawable?,
                     fallbackTint: Int = Color.TRANSPARENT, placeholderTint: Int = Color.TRANSPARENT, errorTint: Int = Color.TRANSPARENT) {

	val fallbackDrawable = abbrText?.let {
		TextDrawable.builder()
			.beginConfig()
			.toUpperCase()
			.textColor(abbrTextColor)
			.fontSize(abbrSize.toInt())
			.bold()
			.endConfig()
			.buildRound(ImageUtils.createAbbreviation(it), abbrBackColor)
	} ?: fallback ?: placeholder

	when (data) {
		is Uri -> {
			if (data.isNotEmpty()) {
				val request = Glide.with(imageView.context)
					.load(data)

				if (asCircle)
					request.optionalCircleCrop()

				fallback?.let {
					request.fallback(
						if (fallbackTint != Color.TRANSPARENT)
							ImageUtils.setTint(it, fallbackTint)
						else it)
				}

				placeholder?.let {
					request.placeholder(
						if (placeholderTint != Color.TRANSPARENT)
							ImageUtils.setTint(it, placeholderTint)
						else it)
				}

				error?.let {
					request.error(
						if (errorTint != Color.TRANSPARENT)
							ImageUtils.setTint(it, errorTint)
						else it)
				} ?: fallbackDrawable?.let {
					request.error(it)
				}

				request.into(imageView)
			}
			else fallbackDrawable
				?.let { imageView.setImageDrawable(it) }
				?: imageView.setImageURI(null)
		}
		is String -> setImageResource(imageView, Uri.parse(data), asCircle,
			abbrText, abbrSize, abbrTextColor, abbrBackColor,
			fallback, placeholder, error,
			fallbackTint, placeholderTint, errorTint)
		is Drawable -> imageView.setImageDrawable(data)
		is Int -> imageView.setImageResource(data)
		else -> fallbackDrawable
			?.let { imageView.setImageDrawable(it) }
			?: imageView.setImageURI(null)
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

@BindingAdapter("backgroundTint")
fun setBackgroundTintCompat(view: DrawableCompatTextView, color: Int) = view.setBackgroundTint(color)
@BindingAdapter("drawableStart")
fun setDrawableStart(imageView: DrawableCompatTextView, drawable: Drawable) = imageView.setDrawableStart(drawable)
@BindingAdapter("drawableTop")
fun setDrawableTop(imageView: DrawableCompatTextView, drawable: Drawable) = imageView.setDrawableTop(drawable)
@BindingAdapter("drawableEnd")
fun setDrawableEnd(imageView: DrawableCompatTextView, drawable: Drawable) = imageView.setDrawableEnd(drawable)
@BindingAdapter("drawableBottom")
fun setDrawableBottom(imageView: DrawableCompatTextView, drawable: Drawable) = imageView.setDrawableBottom(drawable)
@BindingAdapter("drawableStart")
fun setDrawableStart(imageView: DrawableCompatTextView, @DrawableRes resId: Int) = imageView.setDrawableStart(resId)
@BindingAdapter("drawableTop")
fun setDrawableTop(imageView: DrawableCompatTextView, @DrawableRes resId: Int) = imageView.setDrawableTop(resId)
@BindingAdapter("drawableEnd")
fun setDrawableEnd(imageView: DrawableCompatTextView, @DrawableRes resId: Int) = imageView.setDrawableEnd(resId)
@BindingAdapter("drawableBottom")
fun setDrawableBottom(imageView: DrawableCompatTextView, @DrawableRes resId: Int) = imageView.setDrawableBottom(resId)

@BindingAdapter("android:onProgressChanged")
fun onProgressChanged(view: SeekBar, action: Runnable) {
	view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
		override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
			if (fromUser)
				action.run()
		}
		override fun onStartTrackingTouch(seekBar: SeekBar?) {}
		override fun onStopTrackingTouch(seekBar: SeekBar?) {}
	})
}