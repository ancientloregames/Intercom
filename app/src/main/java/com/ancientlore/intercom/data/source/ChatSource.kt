package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat

interface ChatSource : DataSource<String, Chat> {

	fun updateItem(item: Chat, callback: RequestCallback<Any> = RequestCallbackAny)

	fun getBroadcasts(callback: RequestCallback<List<Chat>>)
}