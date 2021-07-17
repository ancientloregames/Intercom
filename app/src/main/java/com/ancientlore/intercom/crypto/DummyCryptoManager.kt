package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message

object DummyCryptoManager: CryptoManager {

	override fun encrypt(message: Message, callback: RequestCallback<Any>) {}

	override fun decryptMessages(messages: List<Message>, callback: RequestCallback<Any>) {
		callback.onSuccess(EmptyObject)
	}

	override fun decryptChats(chats: List<Chat>, callback: RequestCallback<Any>) {
		callback.onSuccess(EmptyObject)
	}
}