package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact

object ContactRepository : ContactSource { //TODO Cache Source usage

	private var remoteSource: ContactSource? = null

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		remoteSource?.getAll(callback)
	}

	override fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>) {
		remoteSource?.addAll(contacts, callback)
	}

	override fun update(contacts: List<Contact>, callback: RequestCallback<Any>?) {
		remoteSource?.update(contacts, callback)
	}

	override fun attachContactListener(id: String, callback: RequestCallback<Contact>) {
		remoteSource?.attachContactListener(id, callback)
	}

	override fun detachListeners() {
		remoteSource?.detachListeners()
	}

	fun setRemoteSource(source: ContactSource) {
		remoteSource = source
	}
}