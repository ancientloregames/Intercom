package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.data.model.Message
import io.reactivex.Observable
import io.reactivex.Single

interface MessageSource : DataSource<String, Message> {

	fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {}

	fun updateMessageUri(id: String, uri: Uri): Single<Any> { return Single.error(EmptyResultException) }

	fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any> = RequestCallbackAny) {}

	fun setMessageStatusReceived(id: String, callback: RequestCallback<Any> = RequestCallbackAny) {}

	fun getNextPage(): Single<List<Message>> { return Single.error(EmptyResultException) }

	fun getNextPage(callback: RequestCallback<List<Message>>) {}

	fun setPaginationLimit(limit: Long)

	fun attachChangeListener(): Observable<ListChanges<Message>> { return Observable.error(EmptyResultException) }
}