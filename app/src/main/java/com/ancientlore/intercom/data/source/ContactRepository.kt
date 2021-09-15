package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.cache.CacheContactSource
import com.ancientlore.intercom.data.source.dummy.DummyContactSource
import com.ancientlore.intercom.utils.Utils

object ContactRepository : ContactSource {

	private var remoteSource: ContactSource = DummyContactSource
	private var localSource: ContactSource? = null
	private val cacheSource = CacheContactSource

	override fun getSourceId() = remoteSource.getSourceId()

	override fun getAll(callback: RequestCallback<List<Contact>>) {

		remoteSource.getAll(object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				cacheSource.reset(result)
				localSource?.addItems(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {

		remoteSource.getItem(id, object : RequestCallback<Contact> {

			override fun onSuccess(result: Contact) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Contact>>) {

		remoteSource.getItems(ids, object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				cacheSource.addItems(result)
				localSource?.addItems(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getItems(ids, object : RequestCallback<List<Contact>> {

					override fun onSuccess(result: List<Contact>) {
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						callback.onSuccess(emptyList())
					}
				})
			}
		})
	}

	override fun addItem(item: Contact, callback: RequestCallback<String>) {

		remoteSource.addItem(item, object : RequestCallback<String> {

			override fun onSuccess(result: String) {
				cacheSource.addItem(item)
				localSource?.addItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {

		remoteSource.addItems(items, object : RequestCallback<List<String>> {

			override fun onSuccess(result: List<String>) {
				cacheSource.addItems(items)
				localSource?.addItems(items)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {

		remoteSource.update(items, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItems(items)
				localSource?.update(items)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {

		remoteSource.update(item, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItem(item)
				localSource?.update(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				cacheSource.reset(result)
				localSource?.addItems(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>) : RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Contact> {

			override fun onSuccess(result: Contact) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	fun setRemoteSource(source: ContactSource) {
		if (remoteSource == source)
			return

		remoteSource.clean()
		cacheSource.clear()

		remoteSource = source

		localSource?.let {
			if (source.getSourceId() != it.getSourceId()) {
				it.clean()
				localSource = null
			}
		}
	}

	fun setLocalSource(source: ContactSource) {
		if (localSource == source)
			return

		localSource?.clean()

		localSource = source

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource.clean()
			remoteSource = DummyContactSource
		}
	}

	private fun getAllFallback(callback: RequestCallback<List<Contact>>) {

		cacheSource.getAll(object : RequestCallback<List<Contact>> {

			override fun onSuccess(result: List<Contact>) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getAll(object : RequestCallback<List<Contact>> {

						override fun onSuccess(result: List<Contact>) {
							cacheSource.reset(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}

	private fun getItemFallback(id: String, callback: RequestCallback<Contact>) {

		cacheSource.getItem(id, object : RequestCallback<Contact> {

			override fun onSuccess(result: Contact) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<Contact> {

						override fun onSuccess(result: Contact) {
							cacheSource.addItem(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}
}