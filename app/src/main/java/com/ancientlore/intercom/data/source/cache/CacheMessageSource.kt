package com.ancientlore.intercom.data.source.cache

import android.net.Uri
import com.ancientlore.intercom.data.model.Message

object CacheMessageSource : CacheSource<String, Message>() {

	fun updateMessageUri(id: String, uri: Uri) {

		cache[id]?.attachUrl = uri.toString()
	}

	fun setMessageStatusReceived(id: String) {

		cache[id]?.status = Message.STATUS_RECEIVED
	}
}