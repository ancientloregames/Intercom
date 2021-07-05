package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.cache.CacheChatSource
import com.ancientlore.intercom.data.source.dummy.DummyChatSource
import com.ancientlore.intercom.utils.Utils

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource = DummyChatSource
	private val cacheSource = CacheChatSource

	override fun getAll(callback: RequestCallback<List<Chat>>) {

		remoteSource.getAll(object : RequestCallback<List<Chat>> {
			override fun onSuccess(result: List<Chat>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				callback.onSuccess(cacheSource.getAll())
			}
		})
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {

		remoteSource.getItem(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
				cacheSource.putItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getItem(id)
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException())
			}
		})
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {

		remoteSource.addItem(item, object : RequestCallback<String> {

			override fun onSuccess(result: String) {
				cacheSource.putItem(item)
				callback.onSuccess(result)

			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun deleteItem(chatId: String, callback: RequestCallback<Any>) {

		remoteSource.deleteItem(chatId, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.deleteItem(chatId)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {

		remoteSource.updateItem(item, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	fun setRemoteSource(source: ChatSource) {
		remoteSource = source
	}
}