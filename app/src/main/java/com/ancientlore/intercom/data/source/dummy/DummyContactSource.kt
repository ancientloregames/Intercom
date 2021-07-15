package com.ancientlore.intercom.data.source.dummy

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.EmptyResultException

object DummyContactSource : ContactSource {

	override fun getSourceId() = ""

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: Contact, callback: RequestCallback<String>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {
		callback.onFailure(EmptyResultException)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}
}