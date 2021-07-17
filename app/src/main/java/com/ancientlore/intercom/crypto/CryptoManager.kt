package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message

interface CryptoManager {

	fun encrypt(message: Message, callback: RequestCallback<Any>)

	fun decryptMessages(messages: List<Message>, callback: RequestCallback<Any>)

	fun decryptChats(chats: List<Chat>, callback: RequestCallback<Any>)
}