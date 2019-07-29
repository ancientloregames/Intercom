package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.cache.CacheMessageSource

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource? = null
	private val cacheSource = CacheMessageSource

	override fun getAll(callback: RequestCallback<List<Message>>) {
		if (cacheSource.isNotEmpty())
			callback.onSuccess(cacheSource.getAll())
		else remoteSource?.getAll(object : RequestCallback<List<Message>> {
			override fun onSuccess(result: List<Message>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		remoteSource?.addMessage(message, callback)
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>?) {
		remoteSource?.updateMessageUri(messageId, uri, callback)
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>?) {
		remoteSource?.setMessageStatusReceived(id, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) {
		if (cacheSource.isNotEmpty())
			callback.onSuccess(cacheSource.getAll())
		remoteSource?.attachListener(object : RequestCallback<List<Message>> {
			override fun onSuccess(result: List<Message>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun detachListener() {
		remoteSource?.detachListener()
	}

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source
	}
}