package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact

object ContactRepository : ContactSource {

	private var remoteSource: ContactSource? = null

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		remoteSource?.getAll(callback)
	}

	override fun addAll(contacts: List<Contact>) {
		remoteSource?.addAll(contacts)
	}

	fun setRemoteSource(source: ContactSource) {
		remoteSource = source
	}

}