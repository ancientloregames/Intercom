package com.ancientlore.intercom.data.source.dummy

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.utils.Utils

object DummyContactSource : ContactSource {

	const val TAG = "DummyContactSource"

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		Utils.logError("${TAG}.getAll")
	}

	override fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>) {
		Utils.logError("${TAG}.addAll")
	}

	override fun update(contacts: List<Contact>, callback: RequestCallback<Any>) {
		Utils.logError("${TAG}.update")
	}

	override fun update(contact: Contact, callback: RequestCallback<Any>) {
		Utils.logError("${TAG}.update")
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>): RepositorySubscription {
		Utils.logError("${TAG}.attachListener")

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}

	override fun attachListener(
		contactId: String,
		callback: RequestCallback<Contact>
	): RepositorySubscription {
		Utils.logError("${TAG}.attachListener.contact")

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}
}