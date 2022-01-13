package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.utils.Identifiable
import io.reactivex.Observable
import io.reactivex.Single

interface DataSource<I, T: Identifiable<I>> {

	fun clean() {}

	fun getSourceId(): String  // for sources consistency check mainly

	fun getAll(): Single<List<T>> { return Single.error(EmptyResultException) }

	fun getAll(callback: RequestCallback<List<T>>) {}

	fun getItem(id: I): Single<List<T>> { return Single.error(EmptyResultException) }

	fun getItem(id: I, callback: RequestCallback<T>) {}

	/**
	 * @param callback onSuccess param - assigned id for the message
	 */
	fun addItem(item: T, callback: RequestCallback<I> = object : CrashlyticsRequestCallback<I>() {}) {}

	fun addItem(item: T): Single<I> { return Single.error(EmptyResultException) }

	/**
	 * @param callback onSuccess param - assigned ids for the messages
	 */
	fun addItems(items: List<T>, callback: RequestCallback<List<I>> =  object : CrashlyticsRequestCallback<List<I>>() {}) {}

	fun deleteItem(id: I, callback: RequestCallback<Any> = RequestCallbackAny) {}

	fun attachListener(callback: RequestCallback<List<T>>) : RepositorySubscription = DummyRepositorySubscription

	fun attachListener(): Observable<List<T>> { return Observable.error(EmptyResultException) }

	fun attachListener(id: I, callback: RequestCallback<T>) : RepositorySubscription = DummyRepositorySubscription
}