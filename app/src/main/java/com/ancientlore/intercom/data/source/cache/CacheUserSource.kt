package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.User

object CacheUserSource : CacheSource<User>() {

	fun reset(newChats: List<User>) {
		cache.clear()
		newChats.forEach {
			cache[it.phone] = it
		}
	}
}