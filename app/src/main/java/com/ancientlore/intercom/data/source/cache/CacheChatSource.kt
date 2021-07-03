package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Chat

object CacheChatSource : CacheSource<Chat>() {

	fun putItem(item: Chat) = cache.put(item.id, item)

	fun deleteItem(item: Chat) = deleteItem(item.id)

	fun updateItem(item: Chat): Boolean {
		return getItem(item.id)
			?.let {
				val updatedItem = Chat(
					id = it.id,
					name = if (item.name.isNotEmpty()) item.name else it.name,
					iconUrl = if (item.iconUrl.isNotEmpty()) item.iconUrl else it.iconUrl,
					initiatorId = it.initiatorId,
					participants = if (item.participants.isNotEmpty()) item.participants else it.participants,
					lastMsgTime = it.lastMsgTime,
					lastMsgText = it.lastMsgText
				)
				putItem(updatedItem)
				true
			} ?: false
	}

	fun reset(newChats: List<Chat>) {
		cache.clear()
		newChats.forEach {
			cache[it.id] = it
		}
	}
}