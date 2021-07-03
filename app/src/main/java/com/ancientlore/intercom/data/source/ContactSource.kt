package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.data.model.Contact

interface ContactSource : DataSource<Contact> {

	fun addAll(contacts: List<Contact>, callback: RequestCallback<Any> = RequestCallbackAny)

	fun update(contacts: List<Contact>, callback: RequestCallback<Any> = RequestCallbackAny)

	fun update(contact: Contact, callback: RequestCallback<Any> = RequestCallbackAny)

	fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription

	fun attachListener(contactId: String, callback: RequestCallback<Contact>) : RepositorySubscription
}