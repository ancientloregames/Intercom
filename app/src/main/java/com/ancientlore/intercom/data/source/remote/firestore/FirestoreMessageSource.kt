package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource
import com.google.firebase.firestore.ListenerRegistration

class FirestoreMessageSource(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {
	internal companion object  {
		private const val TAG = "FirestoreMessageSource"

		private const val CHATS = "chats"
		private const val MESSAGES = "messages"
	}

	private val messagesCollection get() = db.collection(CHATS).document(chatId).collection(MESSAGES)

	private var changeListener: ListenerRegistration? = null

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

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		messagesCollection.add(message)
			.addOnSuccessListener { callback?.onSuccess(it.id) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) {
		changeListener = messagesCollection
			.orderBy("timestamp")
			.addSnapshotListener { snapshot, e ->
				if (e != null) {
					callback.onFailure(e)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					deserialize(snapshot)
						.takeIf { it.all { msg -> msg.hasTimestamp() } }
						?.let { callback.onSuccess(it)  }
						?: callback.onFailure(EmptyResultException("$TAG: empty"))
				}
			}
	}

	override fun detachListener() {
		changeListener?.remove()
	}
}