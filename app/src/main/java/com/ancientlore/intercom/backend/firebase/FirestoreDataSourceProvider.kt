package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.DataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreChatSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreContactSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreMessageSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreUserSource

object FirestoreDataSourceProvider
	: DataSourceProvider {

	override fun getUserSource(userId: String) = FirestoreUserSource(userId)

	override fun getChatSource(userId: String) = FirestoreChatSource.getInstance(userId)

	override fun getMessageSource(chatId: String) = FirestoreMessageSource(chatId)

	override fun getContactSource(userId: String) = FirestoreContactSource.getInstance(userId)
}