package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message

object Base64CryptoManager : CryptoManager {

	override fun encrypt(message: Message, callback: RequestCallback<Any>) {

		message.text = encrypt(message.text)
		callback.onSuccess(EmptyObject)
	}

	override fun decryptMessages(messages: List<Message>, callback: RequestCallback<Any>) {

		for (message in messages) {
			message.text = decrypt(message.text)
		}
		callback.onSuccess(EmptyObject)
	}

	override fun decryptChats(chats: List<Chat>, callback: RequestCallback<Any>) {

		for (chat in chats) {
			chat.lastMsgText = decrypt(chat.lastMsgText)
		}
		callback.onSuccess(EmptyObject)
	}

	private fun encrypt(text: String) = CryptoUtils.encodeToString(text)

	private fun decrypt(text: String) = CryptoUtils.decodeToString(text)
}