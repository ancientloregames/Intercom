package com.ancientlore.intercom.frontend

import android.content.Context
import com.ancientlore.intercom.backend.DataSourceProvider
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.data.source.local.room.*

class RoomDataSourceProvider(context: Context) : DataSourceProvider {

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
}