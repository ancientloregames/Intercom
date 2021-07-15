package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.utils.Identifiable

interface DataSource<I, T: Identifiable<I>> {

	fun getSourceId(): String  // for sources consistency check mainly

	fun getAll(callback: RequestCallback<List<T>>)

	fun getItem(id: I, callback: RequestCallback<T>)

	/**
	 * @param callback onSuccess param - assigned id for the message
	 */
	fun addItem(item: T, callback: RequestCallback<I> = object : CrashlyticsRequestCallback<I>() {})

	/**
	 * @param callback onSuccess param - assigned ids for the messages
	 */
	fun addItems(items: List<T>, callback: RequestCallback<List<I>> =  object : CrashlyticsRequestCallback<List<I>>() {})

	fun deleteItem(id: I, callback: RequestCallback<Any> = RequestCallbackAny)

	fun attachListener(callback: RequestCallback<List<T>>) : RepositorySubscription

	fun attachListener(id: I, callback: RequestCallback<T>) : RepositorySubscription
}