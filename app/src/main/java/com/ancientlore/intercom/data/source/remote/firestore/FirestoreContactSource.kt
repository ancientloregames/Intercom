package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.ListenerRegistration

class FirestoreContactSource private constructor(private val userId: String)
: FirestoreSource<Contact>(), ContactSource {

	internal companion object : SingletonHolder<FirestoreContactSource, String>(
		{ userId -> FirestoreContactSource(userId) }) {
		private const val TAG = "FirestoreMessageSource"

		private const val USERS = "users"
		private const val CONTACTS = "contacts"
	}

	private val userContacts get() = db.collection(USERS).document(userId).collection(CONTACTS)

	private var contactChangesListener: ListenerRegistration? = null

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
			.addOnSuccessListener { users ->
				val list = contacts.toMutableList()
				users.forEach { user ->
					val iter = list.listIterator()
					while (iter.hasNext()) {
						val contact = iter.next()
						if (user.id == contact.phone) {
							userContacts.document(user.id).set(contact)
							iter.remove()
						}
					}
				}
				callback.onSuccess(EmptyObject)
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun attachContactListener(id: String, callback: RequestCallback<Contact>) {
		contactChangesListener = userContacts.document(id)
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					callback.onFailure(error)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					deserialize(snapshot)
						?.let { callback.onSuccess(it)  }
				}
			}
	}

	override fun detachListeners() {
		contactChangesListener?.remove()
	}
}