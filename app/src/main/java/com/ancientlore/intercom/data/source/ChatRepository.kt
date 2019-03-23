package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource? = null

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		remoteSource?.getAll(callback)
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		remoteSource?.getItem(id, callback)
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		remoteSource?.addItem(item, callback)
	}

	override fun createDialog(recipientId: String, callback: RequestCallback<String>) {
		remoteSource?.createDialog(recipientId, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) {
		remoteSource?.attachListener(callback)
	}

	override fun detachListener() {
		remoteSource?.detachListener()
	}

	fun setRemoteSource(source: ChatSource) {
		remoteSource = source
	}

}