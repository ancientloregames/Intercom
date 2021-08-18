package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.source.DataSource
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.Identifiable
import java.util.*

abstract class CacheSource<I, T: Identifiable<I>> : DataSource<I, T> {

	protected val cache: MutableMap<I, T> = Collections.synchronizedMap(HashMap())

	fun isNotEmpty() = cache.isNotEmpty()

	fun isEmpty() = cache.isEmpty()

	override fun getAll(callback: RequestCallback<List<T>>) {
		if (isNotEmpty())
			callback.onSuccess(cache.values.toList())
		else
			callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: I, callback: RequestCallback<T>) {
		cache[id]
			?.let { callback.onSuccess(it) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: T, callback: RequestCallback<I>) {
		cache[item.getIdentity()] = item
	}

	override fun addItems(items: List<T>, callback: RequestCallback<List<I>>) {
		items.forEach {
			cache[it.getIdentity()] = it
		}
	}

	override fun deleteItem(id: I, callback: RequestCallback<Any>) {
		cache.remove(id)
	}

	fun deleteItems(items: List<Identifiable<I>>) {
		for (item in items) {
			cache.remove(item.getIdentity())
		}
	}

	fun clear() = cache.clear()

	fun reset(newItems: List<T>) {
		cache.clear()
		newItems.forEach {
			cache[it.getIdentity()] = it
		}
	}

	override fun getSourceId(): String {
		TODO("Unused")
	}

	override fun attachListener(callback: RequestCallback<List<T>>): RepositorySubscription {
		TODO("Unused")
	}

	override fun attachListener(id: I, callback: RequestCallback<T>): RepositorySubscription {
		TODO("Unused")
	}
}