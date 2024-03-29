package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_TIME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NEW_MSG_COUNT
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TYPE
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException

open class FirestoreChatSource protected constructor(protected val userId: String)
	: FirestoreSource<Chat>(), ChatSource {

	protected val userChats get() = db.collection(USERS).document(userId).collection(CHATS)

	override fun equals(other: Any?): Boolean {
		return other is FirestoreChatSource && other.userId == userId
	}

	override fun clean() {
		cleanInternal()
		super.clean()
	}

	override fun getObjectClass() = Chat::class.java

	override fun getWorkerThreadName() = "fsChatSource_thread"

	override fun getSourceId() = userId

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		userChats.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Chat>>) {
		userChats
			.whereIn(FieldPath.documentId(), ids.toList())
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		userChats.document(id).get()
			.addOnSuccessListener { snapshot ->
				exec {
					deserialize(snapshot)
						?.let { callback.onSuccess(it) }
						?: callback.onFailure(EmptyResultException)
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {
		if (items.isEmpty()) {
			callback.onSuccess(emptyList())
			return
		}
		val remoteChats = ArrayList<String>(items.size)
		val lastChatId = items.last().id
		for (item in items) {
			db.collection(CHATS).add(item)
				.addOnSuccessListener {
					remoteChats.add(it.id)
					if (item.id == lastChatId) {
						exec { callback.onSuccess(remoteChats) }
					}
				}
				.addOnFailureListener { exec { callback.onFailure(it) } }
		}
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		db.collection(CHATS).add(item)
			.addOnSuccessListener { exec { callback.onSuccess(it.id) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		db.collection(CHATS)
			.document(id)
			.delete()
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		db.collection(CHATS)
			.document(item.id)
			.set(HashMap<String, Any>().apply {
				if (item.name.isNotEmpty())
					put(FIELD_NAME, item.name)
				if (item.iconUrl.isNotEmpty())
					put(FIELD_ICON_URL, item.iconUrl)
			}, SetOptions.merge())
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) }
			}
	}

	override fun setMessageRecieved(id: String, callback: RequestCallback<Any>) {

		userChats.document(id)
			.update(FIELD_NEW_MSG_COUNT, 0)
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getBroadcasts(callback: RequestCallback<List<Chat>>) {
		db.collection(CHATS)
			.whereEqualTo(FIELD_TYPE, Chat.TYPE_BROADCAST)
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>) : RepositorySubscription {
		val registration = userChats
			.orderBy(FIELD_LAST_MSG_TIME)
			.addSnapshotListener { snapshot, error ->
				exec {
					if (error != null)
						callback.onFailure(error)
					else if (snapshot != null)
						callback.onSuccess(deserialize(snapshot))
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {

		val registration = userChats
			.document(id)
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> deserialize(snapshot)
							?.let { callback.onSuccess(it) }
							?: callback.onFailure(RuntimeException("Failed to deserialize chat: $id"))
						else -> callback.onFailure(EmptyResultException)
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}
}