package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import io.reactivex.Single

object Base64CryptoManager : CryptoManager {

	override fun encrypt(message: Message): Single<Message> {

		return Single.create { callback ->
			message.text = encrypt(message.text)
			callback.onSuccess(message)
		}
	}

	override fun decryptMessages(messages: List<Message>): Single<List<Message>> {

		return Single.create { callback ->
			for (message in messages) {
				message.text = decrypt(message.text)
			}
			callback.onSuccess(messages)
		}
	}

	override fun decryptChats(chats: List<Chat>): Single<List<Chat>> {

		return Single.create { callback ->
			for (chat in chats) {
				chat.lastMsgText = decrypt(chat.lastMsgText)
			}
			callback.onSuccess(chats)
		}
	}

	private fun encrypt(text: String) = CryptoUtils.encodeToString(text)

	private fun decrypt(text: String) = CryptoUtils.decodeToString(text)
}