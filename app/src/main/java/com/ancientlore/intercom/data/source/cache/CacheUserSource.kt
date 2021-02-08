package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.User

object CacheUserSource {

	private val cache: MutableMap<String, User> = HashMap()

	fun isEmpty() = cache.isEmpty()

	fun isNotEmpty() = isEmpty().not()

	fun getAll() = cache.values.toList()

	fun reset(newChats: List<User>) {
		cache.clear()
		newChats.forEach {
			cache[it.phone] = it
		}
	}
}