package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RemoteDataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.*
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreSignalSource

object FirestoreDataSourceProvider
	: RemoteDataSourceProvider {

	override fun getUserSource(userId: String) = FirestoreUserSource(userId)

	override fun getChatSource(userId: String) = FirestoreChatSourceNoCF(userId)

	override fun getMessageSource(chatId: String) = FirestoreMessageSourceNoCF(chatId)

	override fun getContactSource(userId: String) = FirestoreContactSource(userId)

	override fun getSignalKeychainSource(userId: String) = FirestoreSignalSource(userId)
}