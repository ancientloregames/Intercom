package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.cache.CacheChatSource

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource? = null
	private val cacheSource = CacheChatSource

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		if (cacheSource.isNotEmpty())
			callback.onSuccess(cacheSource.getAll())
		else {
			remoteSource?.getAll(object : RequestCallback<List<Chat>> {
				override fun onSuccess(result: List<Chat>) {
					resetCache(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		cacheSource.getItem(id)
			?.let { callback.onSuccess(it) }
			?:run {
				remoteSource?.getItem(id, object : RequestCallback<Chat> {
					override fun onSuccess(result: Chat) {
						cacheSource.addItem(result)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						callback.onFailure(error)
					}
				})
			}
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		remoteSource?.addItem(item, callback)
	}

	override fun createDialog(recipientId: String, callback: RequestCallback<String>) {
		remoteSource?.createDialog(recipientId, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) {
		if (cacheSource.isNotEmpty())
			callback.onSuccess(cacheSource.getAll())
		else {
			remoteSource?.attachListener(object : RequestCallback<List<Chat>> {
				override fun onSuccess(result: List<Chat>) {
					resetCache(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
		}
	}

	override fun detachListener() {
		remoteSource?.detachListener()
	}

	fun setRemoteSource(source: ChatSource) {
		remoteSource = source
	}

	private fun resetCache(newNotes: List<Chat>) {
		cacheSource.reset(newNotes)
	}
}