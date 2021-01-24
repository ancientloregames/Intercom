package com.ancientlore.intercom.data.source.remote.firestore

import android.util.Log
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class FirestoreChatSourceNoCF private constructor(private val userId: String)
	: FirestoreChatSource(userId), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSourceNoCF, String>(
		{ userId -> FirestoreChatSourceNoCF(userId) }) {
		private const val TAG = "FirestoreChatSourceNoCF"
	}

	private val userChats get() = db.collection("users").document(userId).collection("chats")

	private var changeListener: ListenerRegistration? = null

	override fun getObjectClass() = Chat::class.java

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		userChats.get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		userChats.document(id).get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot)
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: no chat with id $id"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		db.collection("chats").add(item)
			.addOnSuccessListener {
				val initialDate = Date(0)

				if (item.name.isEmpty()) { // Personal chat
					db.collection("users")
						.document(item.participants[0])
						.collection("chats")
						.document(item.participants[1])
						.set(hashMapOf(
							"id" to it.id,
							"lastMsgText" to "",
							"lastMsgTime" to initialDate,
							"name" to item.participants[1]
						))
						.addOnFailureListener { error -> Log.d(TAG, "Failure 1: ${error.message}") }
					db.collection("users")
						.document(item.participants[1])
						.collection("chats")
						.document(item.participants[0])
						.set(hashMapOf(
							"id" to it.id,
							"lastMsgText" to "",
							"lastMsgTime" to initialDate,
							"name" to item.participants[0]
						))
						.addOnFailureListener { error -> Log.d(TAG, "Failure 2: ${error.message}") }
				}
				else {
					for (participant in item.participants) {
						db.collection("users")
							.document(participant)
							.collection("chats")
							.document(it.id)
							.set(hashMapOf(
								"id" to it.id,
								"lastMsgText" to "",
								"lastMsgTime" to initialDate,
								"name" to item.name
							))
					}
				}

				callback?.onSuccess(it.id)
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) {
		changeListener = userChats
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
}