package com.ancientlore.intercom.utils.extensions

import android.content.Context
import java.io.File

fun Context.getAudioMessagesDir(): File? {
	val dir = File(filesDir, "messages")
	return if (dir.isDirectory || dir.mkdirs()) {
		dir
	} else null
}

fun Context.createAudioMessageFile(name: String = "msg_${System.currentTimeMillis()}.3gp"): File? {
	return getAudioMessagesDir()?.let { dir ->
		val file = File(dir, name)
		if (file.exists() || file.createNewFile())
			file
		else null
	}
}