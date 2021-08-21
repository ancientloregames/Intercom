package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.remote.firestore.C.CONTACTS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.SingletonHolder
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException
import kotlin.collections.HashMap

class FirestoreContactSource private constructor(private val userId: String)
: FirestoreSource<Contact>(), ContactSource {

	internal companion object : SingletonHolder<FirestoreContactSource, String>(
		{ userId -> FirestoreContactSource(userId) })

	private val userContacts get() = db.collection(USERS).document(userId).collection(CONTACTS)

	override fun getObjectClass() = Contact::class.java

	override fun getWorkerThreadName() = "fsContactSource_thread"

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		userContacts.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getSourceId() = userId

	override fun addItem(item: Contact, callback: RequestCallback<String>) {

		db.collection(USERS)
			.document(item.phone)
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					snapshot.toObject(User::class.java)
						?.let {
							item.iconUrl = it.iconUrl
							userContacts.document(it.phone).set(item)
								.addOnSuccessListener { exec { callback.onSuccess(item.phone) } }
								.addOnFailureListener { exec { callback.onFailure(it) } }
						}
						?: exec { callback.onFailure(EmptyResultException) }
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {

		db.collection(USERS).get()
			.addOnSuccessListener { snapshot ->
				exec {
					val list = items.toMutableList()
					snapshot.toObjects(User::class.java).forEach { user ->
						val iter = list.listIterator()
						while (iter.hasNext()) {
							val contact = iter.next()
							if (user.phone == contact.phone) {
								contact.iconUrl = user.iconUrl
								userContacts.document(user.phone).set(contact)
								iter.remove()
							}
						}
					}
					callback.onSuccess(items.map { it.phone })
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {

		db.collection(USERS)
			.document(id)
			.get()
			.addOnSuccessListener {
				exec {
					deserialize(it)
						?.let { callback.onSuccess(it) }
						?: callback.onFailure(EmptyResultException)
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Contact>>) {

		db.collection(USERS)
			.whereIn(FieldPath.documentId(), ids)
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {

		if (items.isEmpty()) {
			exec { callback.onSuccess(EmptyObject) }
			return
		}

		val lastContactPhone = items.last().phone
		for (contact in items) {
			if (contact.phone.isEmpty()) {
				Utils.logError("Contact phone number shouldn't be empty: ${contact.name}")
				continue
			}

			userContacts.document(contact.phone)
				.set(HashMap<String, Any>().apply {
					if (contact.name.isNotEmpty())
						put(FIELD_NAME, contact.name)
					if (contact.iconUrl.isNotEmpty())
						put(FIELD_ICON_URL, contact.iconUrl)
				}, SetOptions.merge())
				.addOnSuccessListener {
					if (contact.phone == lastContactPhone)
						exec { callback.onSuccess(EmptyObject) }
				}
				.addOnFailureListener { exec { callback.onFailure(it) } }
		}
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {
		if (item.phone.isNotEmpty()) {

			userContacts.document(item.phone)
				.set(HashMap<String, Any>().apply {
					put(FIELD_ICON_URL, item.phone)
					if (item.name.isNotEmpty())
						put(FIELD_NAME, item.name)
					if (item.iconUrl.isNotEmpty())
						put(FIELD_ICON_URL, item.iconUrl)
				}, SetOptions.merge())
				.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
				.addOnFailureListener { exec { callback.onFailure(it) } }
		}
		else Utils.logError("Contact phone number shouldn't be empty: ${item.name}")
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription {

		val registration = userContacts
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

	override fun attachListener(id: String, callback: RequestCallback<Contact>) : RepositorySubscription {

		val registration = userContacts
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