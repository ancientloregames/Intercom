package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.cache.CacheMessageSource
import com.ancientlore.intercom.utils.Utils
import java.lang.RuntimeException

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource? = null
	private val cacheSource = CacheMessageSource

	override fun getAll(callback: RequestCallback<List<Message>>) {

		remoteSource?.getAll(object : RequestCallback<List<Message>> {
			override fun onSuccess(result: List<Message>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		}) ?: run {
			Utils.logError("MessageRepository.getAll(): no remote source attached!")
			callback.onSuccess(cacheSource.getAll())
		}
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		remoteSource?.addMessage(message, callback)
	}

	override fun deleteMessage(messageId: String, callback: RequestCallback<Any>?) {
		remoteSource?.deleteMessage(messageId, callback)
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>?) {
		remoteSource?.updateMessageUri(messageId, uri, callback)
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>?) {
		remoteSource?.setMessageStatusReceived(id, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription {

		return remoteSource
			?.attachListener(object : RequestCallback<List<Message>> {
				override fun onSuccess(result: List<Message>) {
					cacheSource.reset(result)
					callback.onSuccess(result)
				}
				override fun onFailure(error: Throwable) {
					callback.onFailure(error)
				}
			})
			?: run {
				callback.onFailure(RuntimeException("Error! No remote source to attach to in the message repository"))

				return object : RepositorySubscription {
					override fun remove() {
						Utils.logError("MessageRepository.attachListener(): There were no remoteSource! No subscription to remove")
					}
				}
			}
	}

	override fun getChatId() = remoteSource?.getChatId()

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source
	}
}