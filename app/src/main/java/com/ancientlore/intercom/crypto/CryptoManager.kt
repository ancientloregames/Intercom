package com.ancientlore.intercom.crypto

import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import io.reactivex.Single

interface CryptoManager {

	fun encrypt(message: Message): Single<Message> { return Single.never() }

	fun decryptMessages(messages: List<Message>): Single<List<Message>> { return Single.never() }

	fun decryptChats(chats: List<Chat>): Single<List<Chat>> { return Single.never() }
}