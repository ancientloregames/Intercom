package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.data.model.Contact

interface ContactSource : DataSource<String, Contact> {

	fun update(items: List<Contact>, callback: RequestCallback<Any> = RequestCallbackAny)

	fun update(item: Contact, callback: RequestCallback<Any> = RequestCallbackAny)

	fun getItems(ids: List<String>, callback: RequestCallback<List<Contact>>) //TODO move to interface DataSource
}