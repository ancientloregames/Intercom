package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.cache.CacheMessageSource
import com.ancientlore.intercom.data.source.dummy.DummyMessageSource
import com.ancientlore.intercom.utils.Utils

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource = DummyMessageSource
	private val cacheSource = CacheMessageSource

	override fun getAll(callback: RequestCallback<List<Message>>) {

		remoteSource.getAll(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getAll()
					.takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException())
			}
		})
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>) {

		remoteSource.addMessage(message, object : RequestCallback<String> {

			override fun onSuccess(result: String) {
				cacheSource.addItem(Message.createWithId(message, result))
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun deleteMessage(messageId: String, callback: RequestCallback<Any>) {

		remoteSource.deleteMessage(messageId, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.deleteItem(messageId)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {

		remoteSource.updateMessageUri(messageId, uri, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateMessageUri(messageId, uri)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {

		remoteSource.setMessageStatusReceived(id, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.setMessageStatusReceived(id)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Message>> {

			override fun onSuccess(result: List<Message>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun getChatId() = remoteSource.getChatId()

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source
	}
}