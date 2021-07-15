package com.ancientlore.intercom.data.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.utils.DataConverters
import com.ancientlore.intercom.utils.SingletonHolder

@Database(
	entities = [User::class, Chat::class, Contact::class, Message::class],
	version = 1
)
@TypeConverters(DataConverters::class)
abstract class IntercomDatabase : RoomDatabase() {

	abstract fun userDao(): RoomUserDao

	abstract fun chatDao(): RoomChatDao

	abstract fun contactDao(): RoomContactDao

	abstract fun messageDao(): RoomMessageDao

	companion object : SingletonHolder<IntercomDatabase, Context>({
		Room.databaseBuilder(it, IntercomDatabase::class.java, "intercom.db")
			.build()
	})
}