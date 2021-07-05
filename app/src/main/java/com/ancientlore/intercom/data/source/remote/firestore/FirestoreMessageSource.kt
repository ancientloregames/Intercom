package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ATTACH_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TIMESTAMP
import com.ancientlore.intercom.data.source.remote.firestore.C.MESSAGES

open class FirestoreMessageSource(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {

	protected val chatMessages get() = db.collection(CHATS).document(chatId).collection(MESSAGES)

	override fun getObjectClass() = Message::class.java

	override fun getWorkerThreadName() = "fsMessageSource_thread"

	override fun getAll(callback: RequestCallback<List<Message>>) {
		chatMessages
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>) {
		chatMessages
			.add(message)
			.addOnSuccessListener { exec { callback.onSuccess(it.id) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun deleteMessage(messageId: String, callback: RequestCallback<Any>) {
		chatMessages
			.document(messageId)
			.delete()
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {
		chatMessages
			.document(messageId)
			.update(FIELD_ATTACH_URL, uri.toString())
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {
		chatMessages
			.document(id)
			.update(FIELD_STATUS, Message.STATUS_RECEIVED)
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription {
		val registration = chatMessages
			.orderBy(FIELD_TIMESTAMP)
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> callback.onSuccess(deserialize(snapshot))
						else -> callback.onFailure(EmptyResultException())
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}

	override fun getChatId() = chatId
}