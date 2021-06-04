package com.ancientlore.intercom.utils.extensions

import android.net.Uri

fun Uri.isInternal() : Boolean {

	return scheme
		?.takeIf  { it.equals("file", true)
				|| it.equals("content", true) } != null
}

fun Uri.isExternal() : Boolean {

	return isInternal().not()
}