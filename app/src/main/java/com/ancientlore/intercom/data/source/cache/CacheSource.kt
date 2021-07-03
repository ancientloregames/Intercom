package com.ancientlore.intercom.data.source.cache

abstract class CacheSource<T> {

	protected val cache: MutableMap<String, T> = HashMap()

	fun isNotEmpty() = cache.isNotEmpty()

	fun isEmpty() = cache.isEmpty()

	fun getAll() = cache.values.toList()

	fun getItem(id: String) = cache[id]

	fun setItem(id: String, item: T) = cache.put(id, item)

	fun deleteItem(id: String) = cache.remove(id)
}