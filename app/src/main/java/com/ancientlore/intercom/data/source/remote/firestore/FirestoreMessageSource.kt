package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.utils.SingletonHolder

class FirestoreMessageSource private constructor(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {

	internal companion object : SingletonHolder<FirestoreMessageSource, String>(
		{ userId -> FirestoreMessageSource(userId) }) {
		private const val TAG = "FirestoreMessageSource"

		private const val CHATS = "chats"
		private const val MESSAGES = "messages"
	}

	override fun getObjectClass() = Message::class.java

	override fun getAll(callback: RequestCallback<List<Message>>) {
		requestChatMessages()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	private fun requestChatMessages() = db.collection(CHATS).document(chatId).collection(
		MESSAGES
	).get()
}