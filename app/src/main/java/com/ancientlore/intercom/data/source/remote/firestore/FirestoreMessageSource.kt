package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ATTACH_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TIMESTAMP
import com.ancientlore.intercom.data.source.remote.firestore.C.MESSAGES
import com.google.firebase.firestore.ListenerRegistration

open class FirestoreMessageSource(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {

	internal companion object  {
		private const val TAG = "FirestoreMessageSource"
	}

	protected val chatMessages get() = db.collection(CHATS).document(chatId).collection(MESSAGES)

	private var changeListener: ListenerRegistration? = null

	override fun getObjectClass() = Message::class.java

	override fun getAll(callback: RequestCallback<List<Message>>) {
		chatMessages.get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		chatMessages.add(message)
			.addOnSuccessListener { callback?.onSuccess(it.id) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>?) {
		chatMessages.document(messageId).update(FIELD_ATTACH_URL, uri.toString())
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>?) {
		chatMessages.document(id).update(FIELD_STATUS, Message.STATUS_RECEIVED)
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) {
		changeListener = chatMessages
			.orderBy(FIELD_TIMESTAMP)
			.addSnapshotListener { snapshot, e ->
				if (e != null) {
					callback.onFailure(e)
				} else if (snapshot != null) {
					deserialize(snapshot)
						.takeIf { it.all { msg -> msg.id.isNotEmpty() } }
						?.let { callback.onSuccess(it)  }
						?: callback.onFailure(EmptyResultException("$TAG: empty"))
				}
			}
	}

	override fun detachListener() {
		changeListener?.remove()
	}

	override fun getChatId() = chatId
}