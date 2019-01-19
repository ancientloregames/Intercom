package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource? = null

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		remoteSource?.getAll(callback)
	}

	fun setRemoteSource(source: ChatSource) {
		remoteSource = source
	}

}