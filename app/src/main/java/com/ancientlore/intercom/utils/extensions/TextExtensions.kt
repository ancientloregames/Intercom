package com.ancientlore.intercom.utils.extensions

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

fun CharSequence.setColorRegion(text: CharSequence, @ColorInt color: Int): CharSequence {
	return SpannableString(this).apply {
		val start = indexOf(text.toString())
		setSpan(ForegroundColorSpan(color), start, start.plus(text.length), 0)
	}
}

fun CharSequence.setColor(@ColorInt color: Int): CharSequence {
	return SpannableString(this).apply {
		setSpan(ForegroundColorSpan(color), 0, length, 0)
	}
}