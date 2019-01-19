package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message

object MessageRepository : MessageSource {

	private var remoteSource: MessageSource? = null

	override fun getAll(callback: RequestCallback<List<Message>>) {
		remoteSource?.getAll(callback)
	}

	fun setRemoteSource(source: MessageSource) {
		remoteSource = source
	}

}