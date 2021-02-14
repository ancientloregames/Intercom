package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact

interface ContactSource : DataSource<Contact> {

	fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>)

	fun update(contacts: List<Contact>, callback: RequestCallback<Any>? = null)

	fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription

	fun attachListener(contactId: String, callback: RequestCallback<Contact>) : RepositorySubscription
}