package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.ListenerRegistration

class FirestoreChatSource private constructor(private val userId: String)
	: FirestoreSource<Chat>(), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSource, String>(
		{ userId -> FirestoreChatSource(userId) }) {
		private const val TAG = "FirestoreChatSource"

		private const val CHATS = "chats"
	}

	private val chatsCollection get() = db.collection(CHATS)

	private var changeListener: ListenerRegistration? = null

	override fun getObjectClass() = Chat::class.java

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		db.collection("users")
			.document(userId)
			.collection("contacts")
			.whereGreaterThan("lastMsgTime", 0).get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		requestUserChat(id)
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot)
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: no chat with id $id"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		chatsCollection.add(item)
			.addOnSuccessListener { callback?.onSuccess(it.id) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun createDialog(recipientId: String, callback: RequestCallback<String>) {
		db.collection("users")
			.document(userId)
			.collection("dialogs")
			.add(mapOf("recipientId" to recipientId))
			.addOnSuccessListener { callback.onSuccess(it.id) }
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) {
		changeListener = db.collection("users")
			.document(userId)
			.collection("contacts")
			.whereGreaterThan("lastMsgTime", 0)
			.orderBy("lastMsgTime")
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					callback.onFailure(error)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					deserialize(snapshot)
						.takeIf { it.isNotEmpty() }
						?.let { callback.onSuccess(it)  }
				}
			}
	}

	override fun detachListener() {
		changeListener?.remove()
	}

	private fun requestUserChat(id: String) = chatsCollection.document(id).get()
}