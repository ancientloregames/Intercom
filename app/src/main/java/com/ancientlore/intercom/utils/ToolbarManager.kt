package com.ancientlore.intercom.utils

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R

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

	fun enableBackButton(listener: View.OnClickListener) {
		toolbar.setNavigationIcon(R.drawable.ic_nav_back)
		toolbar.setNavigationOnClickListener(listener)
	}
}