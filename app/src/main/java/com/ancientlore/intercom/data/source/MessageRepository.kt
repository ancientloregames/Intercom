package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource? = null

	override fun getAll(callback: RequestCallback<List<Message>>) {
		remoteSource?.getAll(callback)
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		remoteSource?.addMessage(message, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) {
		remoteSource?.attachListener(callback)
	}

	override fun detachListener() {
		remoteSource?.detachListener()
	}

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source
	}
}