package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Chat

object CacheChatSource {

	private val cache: MutableMap<String, Chat> = HashMap()

	fun isNotEmpty() = cache.isNotEmpty()

	fun getAll() = cache.values.toList()

	fun getItem(id: String) = cache[id]

	fun addItem(item: Chat) = cache.put(item.id, item)

	fun deleteItem(chatId: String) = cache.remove(chatId)

	fun deleteItem(item: Chat) = deleteItem(item.id)

	fun reset(newChats: List<Chat>) {
		cache.clear()
		newChats.forEach {
			cache[it.id] = it
		}
	}
}