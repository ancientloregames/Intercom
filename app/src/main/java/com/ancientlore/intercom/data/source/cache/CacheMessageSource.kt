package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Message

object CacheMessageSource {

	private val cache: MutableMap<Long, Message> = HashMap()

	fun isNotEmpty() = cache.isNotEmpty()

	fun getAll() = cache.values.toList()

	fun getItem(id: Long) = cache[id]

	fun addItem(item: Message) = cache.put(item.timestamp, item)

	fun reset(newChats: List<Message>) {
		cache.clear()
		newChats.forEach {
			cache[it.timestamp] = it
		}
	}
}