package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Chat

object CacheChatSource : CacheSource<String, Chat>() {

	fun updateItem(item: Chat): Boolean {
		return cache[item.id]
			?.let {
				if (item.name.isNotEmpty())
					it.name = item.name
				if (item.iconUrl.isNotEmpty())
					it.iconUrl = item.iconUrl
				if (item.participants.isNotEmpty())
					it.participants = item.participants
				if (item.newMsgCount != 0)
					it.newMsgCount = item.newMsgCount
				true
			} ?: false
	}

	fun setMessageRecieved(id: String) {
		cache[id]
			?.let {
				it.newMsgCount = 0
			}
	}
}