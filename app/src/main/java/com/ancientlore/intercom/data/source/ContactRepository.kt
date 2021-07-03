package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.cache.CacheContactSource
import com.ancientlore.intercom.data.source.dummy.DummyContactSource
import com.ancientlore.intercom.utils.Utils

object ContactRepository : ContactSource {

	private var remoteSource: ContactSource = DummyContactSource
	private val cacheSource = CacheContactSource

	override fun getAll(callback: RequestCallback<List<Contact>>) {

		remoteSource.getAll(object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getAll()
					.takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException())
			}
		})
	}

	override fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>) {

		remoteSource.addAll(contacts, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.addItems(contacts)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun update(contacts: List<Contact>, callback: RequestCallback<Any>) {

		remoteSource.update(contacts, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItems(contacts)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun update(contact: Contact, callback: RequestCallback<Any>) {

		remoteSource.update(contact, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItem(contact)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>) : RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Contact> {

			override fun onSuccess(result: Contact) {
				cacheSource.updateItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	fun setRemoteSource(source: ContactSource) {
		remoteSource = source
	}
}