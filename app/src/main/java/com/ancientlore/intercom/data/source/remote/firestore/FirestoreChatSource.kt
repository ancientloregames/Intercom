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
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.SetOptions

open class FirestoreChatSource protected constructor(private val userId: String)
	: FirestoreSource<Chat>(), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSource, String>(
		{ userId -> FirestoreChatSource(userId) })

	protected val userChats get() = db.collection(USERS).document(userId).collection(CHATS)

	override fun getObjectClass() = Chat::class.java

	override fun getWorkerThreadName() = "fsChatSource_thread"

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		userChats.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		userChats.document(id).get()
			.addOnSuccessListener { snapshot ->
				exec {
					deserialize(snapshot)
						?.let { callback.onSuccess(it) }
						?: callback.onFailure(EmptyResultException("no chat with id $id"))
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		db.collection(CHATS).add(item)
			.addOnSuccessListener { exec { callback.onSuccess(it.id) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun deleteItem(chatId: String, callback: RequestCallback<Any>) {
		db.collection(CHATS)
			.document(chatId)
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					deserialize(snapshot)
						?.let { chat ->
							db.collection(USERS)
								.document(userId)
								.collection(CHATS)
								.document(
									if (chat.name.isNotEmpty())
										chat.name
									else
										chat.participants.first { it != userId })
								.delete()
								.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
								.addOnFailureListener { exec { callback.onFailure(it) } }
						}
				}
			}
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
}