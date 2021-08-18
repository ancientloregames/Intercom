package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.cache.CacheMessageSource
import com.ancientlore.intercom.data.source.dummy.DummyMessageSource
import com.ancientlore.intercom.utils.Utils
import java.lang.RuntimeException
import java.util.*

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource = DummyMessageSource
	private var localSource: MessageSource? = null
	private val cacheSource = CacheMessageSource

	private var paginationLimit = C.DEF_MSG_PAGINATION_LIMIT

	private val userId = App.backend.getAuthManager().getCurrentUser().id

	override fun getSourceId() = remoteSource.getSourceId()

	override fun getAll(callback: RequestCallback<List<Message>>) {

		remoteSource.getAll(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {

				App.frontend.getCryptoManager(userId).decryptMessages(result, object : RequestCallback<Any> {

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

	override fun getNextPage(callback: RequestCallback<List<Message>>) {

		remoteSource.getNextPage(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {

				App.frontend.getCryptoManager(userId).decryptMessages(result, object : RequestCallback<Any> {

					override fun onSuccess(ignore: Any) {
						cacheSource.addItems(result)
						localSource?.addItems(result)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
						getNextPageFallback(callback)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getNextPageFallback(callback)
			}
		})
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {

		remoteSource.getItem(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
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

	override fun addItem(item: Message, callback: RequestCallback<String>) {

		// Advanced encryption protocols like Signal make user messages unencryptable by user himself,
		// hence need to store them unencrypted locally
		val originalMessage = item.clone()

		App.frontend.getCryptoManager(item.senderId).encrypt(item, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				remoteSource.addItem(item, object : RequestCallback<String> {

					override fun onSuccess(result: String) {
						originalMessage.id = result
						originalMessage.timestamp = Date(System.currentTimeMillis()) // FIXME dummy
						cacheSource.addItem(originalMessage)
						localSource?.addItem(originalMessage)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) { callback.onFailure(error) }
				})
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {

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

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {

		remoteSource.updateMessageUri(messageId, uri, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateMessageUri(messageId, uri)
				localSource?.updateMessageUri(messageId, uri)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {

		remoteSource.setMessageStatusReceived(id, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.setMessageStatusReceived(id)
				localSource?.setMessageStatusReceived(id)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>): RepositorySubscription {

		return remoteSource.attachChangeListener(object : CrashlyticsRequestCallback<ListChanges<Message>>() {

			override fun onSuccess(result: ListChanges<Message>) {

				val messagesToDecrypt = result.addList.plus(result.modifyList)
				App.frontend.getCryptoManager(userId).decryptMessages(messagesToDecrypt, object : RequestCallback<Any> {

					override fun onSuccess(ignore: Any) {
						callback.onSuccess(result)

						cacheSource.deleteItems(result.removeList)
						//TODO localSource?.deleteItems(result.removeList)

						cacheSource.addItems(result.addList)
						localSource?.addItems(result.addList)

						cacheSource.addItems(result.modifyList)
						localSource?.addItems(result.modifyList)
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
						// TODO
					}
				})
			}
		})
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {

				App.frontend.getCryptoManager(userId).decryptMessages(result, object : RequestCallback<Any> {

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

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
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

	override fun setPaginationLimit(limit: Long) {
		if (limit > 1) {
			this.paginationLimit = limit
			remoteSource.setPaginationLimit(limit)
			localSource?.setPaginationLimit(limit)
		}
		else Utils.logError("Pagination limit must be > 1")
	}

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source

		source.setPaginationLimit(paginationLimit)
		cacheSource.setPaginationLimit(paginationLimit)

		cacheSource.clear()
		localSource?.let {
			if (source.getSourceId() != it.getSourceId())
				localSource = null
		}
	}

	fun setLocalSource(source: MessageSource) {
		localSource = source

		source.setPaginationLimit(paginationLimit)

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource = DummyMessageSource
		}
	}

	private fun getAllFallback(callback: RequestCallback<List<Message>>) {

		cacheSource.getAll(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getAll(object : RequestCallback<List<Message>> {

						override fun onSuccess(result: List<Message>) {
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

	private fun getNextPageFallback(callback: RequestCallback<List<Message>>) {
		callback.onFailure(RuntimeException("Fallback not implemented"))
		// TODO from cache or local source
	}

	private fun getItemFallback(id: String, callback: RequestCallback<Message>) {

		cacheSource.getItem(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<Message> {

						override fun onSuccess(result: Message) {
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