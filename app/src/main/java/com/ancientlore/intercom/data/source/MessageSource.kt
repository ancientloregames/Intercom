package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message

interface MessageSource : DataSource<Message> {

	/**
	 * @param callback onSuccess param - server id for the message
	 */
	fun addMessage(message: Message, callback: RequestCallback<String>?)

	fun attachListener(callback: RequestCallback<List<Message>>)

	fun detachListener()
}