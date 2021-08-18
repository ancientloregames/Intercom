package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.data.model.Message

interface MessageSource : DataSource<String, Message> {

	fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>)

	fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any> = RequestCallbackAny)

	fun setMessageStatusReceived(id: String, callback: RequestCallback<Any> = RequestCallbackAny)

	fun getNextPage(callback: RequestCallback<List<Message>>)

	fun setPaginationLimit(limit: Long)

	fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>) : RepositorySubscription
}