package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import io.reactivex.Single

object DummyCryptoManager: CryptoManager {

	override fun encrypt(message: Message): Single<Message> {
		return Single.create {
			it.onSuccess(message)
		}
	}

	override fun decryptMessages(messages: List<Message>): Single<List<Message>> {
		return Single.create {
			it.onSuccess(messages)
		}
	}

	override fun decryptChats(chats: List<Chat>): Single<List<Chat>> {
		return Single.create {
			it.onSuccess(chats)
		}
	}
}