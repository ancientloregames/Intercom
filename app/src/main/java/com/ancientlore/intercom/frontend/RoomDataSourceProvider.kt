package com.ancientlore.intercom.frontend

import android.content.Context
import com.ancientlore.intercom.data.source.*
import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource
import com.ancientlore.intercom.data.source.local.pref.SharedPrefSignalSource
import com.ancientlore.intercom.data.source.local.room.*

class RoomDataSourceProvider(private val context: Context) : LocalDataSourceProvider {

	private val db = IntercomDatabase.getInstance(context)

	override fun getUserSource(userId: String): UserSource {
		return RoomUserSource(userId, db.userDao())
	}

	override fun getChatSource(userId: String): ChatSource {
		return RoomChatSource(userId, db.chatDao())
	}

	override fun getMessageSource(chatId: String): MessageSource {
		return RoomMessageSource(chatId, db.messageDao())
	}

	override fun getContactSource(userId: String): ContactSource {
		return RoomContactSource(userId, db.contactDao())
	}

	override fun getSignalKeychainSource(userId: String): SignalPrivateKeySource = SharedPrefSignalSource(context)
}