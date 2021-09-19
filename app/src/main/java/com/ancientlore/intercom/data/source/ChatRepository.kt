package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.cache.CacheChatSource
import com.ancientlore.intercom.data.source.dummy.DummyChatSource
import com.ancientlore.intercom.utils.Utils

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource = DummyChatSource
	private var localSource: ChatSource? = null
	private val cacheSource = CacheChatSource

	override fun getSourceId() = remoteSource.getSourceId()

	override fun getAll(callback: RequestCallback<List<Chat>>) {

		remoteSource.getAll(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {

				val userId = App.backend.getAuthManager().getCurrentUserId()
				App.frontend.getCryptoManager(userId).decryptChats(result, object : RequestCallback<Any> {

					override fun onSuccess(ignore: Any) {
						cacheSource.reset(result)
						localSource?.addItems(result)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
						getAllFallback(callback)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {

		remoteSource.getItem(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {

		remoteSource.addItem(item, object : RequestCallback<String> {

			override fun onSuccess(result: String) {
				item.id = result
				cacheSource.addItem(item)
				localSource?.addItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {

		remoteSource.addItems(items, object : RequestCallback<List<String>> {

			override fun onSuccess(result: List<String>) {
				for (i in 0..result.size) {
					items[i].id = result[i]
				}
				cacheSource.addItems(items)
				localSource?.addItems(items)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		remoteSource.deleteItem(id, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.deleteItem(id)
				localSource?.deleteItem(id)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {

		remoteSource.updateItem(item, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItem(item)
				localSource?.updateItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun getBroadcasts(callback: RequestCallback<List<Chat>>) {

		remoteSource.getBroadcasts(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {
				//TODO update local sources
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				//TODO fallback
			}
		})
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {

				val userId = App.backend.getAuthManager().getCurrentUserId()
				App.frontend.getCryptoManager(userId).decryptChats(result, object : RequestCallback<Any> {

					override fun onSuccess(ignore: Any) {
						cacheSource.reset(result)
						localSource?.addItems(result)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
						getAllFallback(callback)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	fun setRemoteSource(source: ChatSource) {
		if (remoteSource == source)
			return

		remoteSource.clean()
		cacheSource.clear()

		remoteSource = source

		localSource?.let {
			if (source.getSourceId() != it.getSourceId()) {
				it.clean()
				localSource = null
			}
		}
	}

	fun setLocalSource(source: ChatSource) {
		if (localSource == source)
			return

		localSource?.clean()

		localSource = source

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource.clean()
			remoteSource = DummyChatSource
		}
	}

	private fun getAllFallback(callback: RequestCallback<List<Chat>>) {

		cacheSource.getAll(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getAll(object : RequestCallback<List<Chat>> {

						override fun onSuccess(result: List<Chat>) {
							cacheSource.reset(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}

	private fun getItemFallback(id: String, callback: RequestCallback<Chat>) {

		cacheSource.getItem(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<Chat> {

						override fun onSuccess(result: Chat) {
							cacheSource.addItem(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}
}