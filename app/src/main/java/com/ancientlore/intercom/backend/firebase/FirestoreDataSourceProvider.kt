package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.DataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.*

object FirestoreDataSourceProvider
	: DataSourceProvider {

	override fun getUserSource(userId: String) = FirestoreUserSource(userId)

	override fun getChatSource(userId: String) = FirestoreChatSourceNoCF.getInstance(userId)

	override fun getMessageSource(chatId: String) = FirestoreMessageNoCF(chatId)

	override fun getContactSource(userId: String) = FirestoreContactSource.getInstance(userId)
}