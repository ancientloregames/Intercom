package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.data.DataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreChatSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreContactSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreMessageSource

object FirestoreDataSourceProvider
	: DataSourceProvider {

	override fun getChatSource(userId: String) = FirestoreChatSource.getInstance(userId)

	override fun getMessageSource(chatId: String) = FirestoreMessageSource(chatId)

	override fun getContactSource(userId: String) = FirestoreContactSource.getInstance(userId)
}