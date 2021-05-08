package com.ancientlore.intercom.data.source

import android.util.Log
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.cache.CacheChatSource
import java.lang.RuntimeException

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource? = null
	private val cacheSource = CacheChatSource

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		remoteSource
			?.getAll(object : RequestCallback<List<Chat>> {
				override fun onSuccess(result: List<Chat>) {
					resetCache(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
			?: callback.onSuccess(cacheSource.getAll())
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		remoteSource
			?.getItem(id, object : RequestCallback<Chat> {
				override fun onSuccess(result: Chat) {
					cacheSource.putItem(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
			?: cacheSource.getItem(id)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException())
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		cacheSource.putItem(item)
		remoteSource?.addItem(item, callback)
	}

	override fun deleteItem(chatId: String, callback: RequestCallback<Any>?) {
		cacheSource.deleteItem(chatId)
		remoteSource?.deleteItem(chatId, callback)
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>?) {
		remoteSource
			?.updateItem(item, object : RequestCallback<Any> {
				override fun onSuccess(result: Any) {
					cacheSource.updateItem(item)
					callback?.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback?.onFailure(error)
				}
			})
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) : RepositorySubscription {
		return remoteSource
			?.attachListener(object : RequestCallback<List<Chat>> {
				override fun onSuccess(result: List<Chat>) {
					resetCache(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
			?: run {
				callback.onFailure(RuntimeException("Error! No remote source to attach to in the chat repository"))

				return object : RepositorySubscription {
					override fun remove() {
						Log.w("ChatRepository",
							"attachListener(): There were no remoteSource! No subscription to remove")
					}
				}
			}
	}

	fun setRemoteSource(source: ChatSource) {
		remoteSource = source
	}

	private fun resetCache(newNotes: List<Chat>) {
		cacheSource.reset(newNotes)
	}
}