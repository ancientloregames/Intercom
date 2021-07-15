package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_FCM_TOKEN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_SEEN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ONLINE
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException

open class FirestoreUserSource(private val userId: String)
	: FirestoreSource<User>(), UserSource {

	protected val user get() = users.document(userId)

	protected val users get() = db.collection(USERS)

	override fun getObjectClass() = User::class.java

	override fun getWorkerThreadName() = "fsUserSource_thread"

	override fun getSourceId() = userId

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {
		user
			.update(FIELD_FCM_TOKEN, token)
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getAll(callback: RequestCallback<List<User>>) {
		users.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {

		db.collection(USERS)
			.document(item.phone)
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(item.phone) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {
		if (items.isEmpty()) {
			callback.onSuccess(emptyList())
			return
		}
		val lastChatId = items.last().id
		for (item in items) {
			db.collection(USERS).add(item)
				.addOnSuccessListener {
					if (item.id == lastChatId) {
						exec { callback.onSuccess(items.map { it.id }) }
					}
				}
				.addOnFailureListener { exec { callback.onFailure(it) } }
		}
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {
		users
			.document(id)
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					deserialize(snapshot)
						?.let { callback.onSuccess(it) }
						?: callback.onFailure(EmptyResultException)
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {
		user
			.set(hashMapOf(FIELD_ICON_URL to uri.toString()), SetOptions.merge())
			.addOnSuccessListener {
				App.backend.getAuthManager().updateUserIconUri(uri, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { exec { callback.onSuccess(result) } }
					override fun onFailure(error: Throwable) { exec { callback.onFailure(error) } }
				})
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {
		user
			.set(hashMapOf(FIELD_NAME to name), SetOptions.merge())
			.addOnSuccessListener {
				App.backend.getAuthManager().updateUserName(name, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { exec { callback.onSuccess(result) } }
					override fun onFailure(error: Throwable) { exec { callback.onFailure(error) } }
				})
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {
		user
			.set(hashMapOf(FIELD_STATUS to status), SetOptions.merge())
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {
		user
			.set(hashMapOf(
				FIELD_LAST_SEEN to FieldValue.serverTimestamp(),
				FIELD_ONLINE to online), SetOptions.merge())
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {
		val registration = users
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> callback.onSuccess(deserialize(snapshot))
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

	override fun attachListener(id: String, callback: RequestCallback<User>) : RepositorySubscription {
		val registration = users
			.document(id)
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> deserialize(snapshot)
							?.let { callback.onSuccess(it) }
							?: callback.onFailure(RuntimeException("Failed to deserialize contact: $id"))
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