package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact

interface ContactSource : DataSource<Contact> {
	fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>)

	fun update(contacts: List<Contact>, callback: RequestCallback<Any>? = null)

	fun attachContactListener(id: String, callback: RequestCallback<Contact>)

	fun detachListeners()
}