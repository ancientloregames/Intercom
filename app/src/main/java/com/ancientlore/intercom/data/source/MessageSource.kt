package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message

interface MessageSource : DataSource<Message> {

	/**
	 * @param callback onSuccess param - server id for the message
	 */
	fun addMessage(message: Message, callback: RequestCallback<String>?)

	fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>?)

	fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>?)

	fun attachListener(callback: RequestCallback<List<Message>>)

	fun detachListener()

	fun getChatId(): String?
}