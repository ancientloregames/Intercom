package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.source.remote.firestore.C.CONTACTS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_SEEN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

class FirestoreContactSource private constructor(private val userId: String)
: FirestoreSource<Contact>(), ContactSource {

	internal companion object : SingletonHolder<FirestoreContactSource, String>(
		{ userId -> FirestoreContactSource(userId) }) {
		private const val TAG = "FirestoreMessageSource"
	}

	private val userContacts get() = db.collection(USERS).document(userId).collection(CONTACTS)

	override fun getObjectClass() = Contact::class.java

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		userContacts.get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException("$TAG: empty"))
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>) {
		db.collection(USERS).get()
			.addOnSuccessListener { snapshot ->
				val list = contacts.toMutableList()
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
				callback.onSuccess(EmptyObject)
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun update(contacts: List<Contact>, callback: RequestCallback<Any>) {

		var success = true

		for (contact in contacts) {
			if (contact.phone.isEmpty()) {
				callback?.onFailure(RuntimeException("Contact phone number shouldn't be empty"))
				continue
			}

			userContacts.document(contact.phone)
				.set(HashMap<String, Any>().apply {
					if (contact.name.isNotEmpty())
						put(FIELD_NAME, contact.name)
					if (contact.lastSeenTime != 0L)
						put(FIELD_LAST_SEEN, contact.name)
					if (contact.iconUrl.isNotEmpty())
						put(FIELD_ICON_URL, contact.iconUrl)
				}, SetOptions.merge())
				.addOnFailureListener {
					callback?.onFailure(it)
					success = false
				}
		}

		if (success)
			callback?.onSuccess(EmptyObject)
	}

	override fun update(contact: Contact, callback: RequestCallback<Any>) {
		if (contact.phone.isNotEmpty()) {

			userContacts.document(contact.phone)
				.set(HashMap<String, Any>().apply {
					if (contact.name.isNotEmpty())
						put(FIELD_NAME, contact.name)
					if (contact.lastSeenTime != 0L)
						put(FIELD_LAST_SEEN, contact.name)
					if (contact.iconUrl.isNotEmpty())
						put(FIELD_ICON_URL, contact.iconUrl)
				}, SetOptions.merge())
				.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
				.addOnFailureListener { callback?.onFailure(it) }
		}
		else callback?.onFailure(RuntimeException("Contact phone number shouldn't be empty"))
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription {

		val registration = userContacts.addSnapshotListener { snapshot, error ->
			if (error != null) {
				callback.onFailure(error)
				return@addSnapshotListener
			}
			else if (snapshot != null) {
				callback.onSuccess(deserialize(snapshot))
			}
		}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>) : RepositorySubscription {

		val registration = userContacts.document(id).addSnapshotListener { snapshot, error ->
			if (error != null) {
				callback.onFailure(error)
				return@addSnapshotListener
			}
			else if (snapshot != null) {
				deserialize(snapshot)
					?.let { callback.onSuccess(it)  }
			}
		}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}
}