package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource

class FirestoreMessageSource(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {

	internal companion object  {
		private const val TAG = "FirestoreMessageSource"

		private const val CHATS = "chats"
		private const val MESSAGES = "messages"
	}

	private val messagesCollection get() = db.collection(CHATS).document(chatId).collection(MESSAGES)

	override fun getObjectClass() = Message::class.java

	override fun getAll(callback: RequestCallback<List<Message>>) {
		messagesCollection.get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}
}