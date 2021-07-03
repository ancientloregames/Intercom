package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.backend.callback.RequestCallbackString
import com.ancientlore.intercom.data.model.Message

interface MessageSource : DataSource<Message> {

	/**
	 * @param callback onSuccess param - server id for the message
	 */
	fun addMessage(message: Message, callback: RequestCallback<String> = RequestCallbackString)

	fun deleteMessage(messageId: String, callback: RequestCallback<Any> = RequestCallbackAny)

	fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any> = RequestCallbackAny)

	fun setMessageStatusReceived(id: String, callback: RequestCallback<Any> = RequestCallbackAny)

	fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription

	fun getChatId(): String?
}