package com.ancientlore.intercom.data.source.cache

import android.net.Uri
import com.ancientlore.intercom.data.model.Message

object CacheMessageSource : CacheSource<Message>() {

	fun addItem(item: Message) = cache.put(item.id, item)

	fun updateMessageUri(id: String, uri: Uri) {

		cache[id]?.run {
			cache[id] = Message(id, timestamp, senderId, text, info, uri.toString(), type, status, progress)
		}
	}

	fun setMessageStatusReceived(id: String) {

		cache[id]?.run {
			cache[id] = Message(id, timestamp, senderId, text, info, attachUrl, type,
				Message.STATUS_RECEIVED, progress)
		}
	}

	fun reset(newChats: List<Message>) {
		cache.clear()
		newChats.forEach {
			cache[it.id] = it
		}
	}
}