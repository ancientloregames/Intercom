package com.ancientlore.intercom.data.source.dummy

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.utils.Utils

object DummyMessageSource : MessageSource {

	const val TAG = "DummyMessageSource"

	override fun getAll(callback: RequestCallback<List<Message>>) {
		Utils.logError("$TAG.getAll")
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>) {
		Utils.logError("""$TAG.addMessage""")
	}

	override fun deleteMessage(messageId: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.deleteMessage")
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateMessageUri")
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.setMessageStatusReceived")
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {
		Utils.logError("$TAG.attachListener")

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}

	override fun getChatId(): String? {
		Utils.logError("$TAG.getChatId")
		return null
	}
}