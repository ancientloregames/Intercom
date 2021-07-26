package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RemoteDataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.*
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreSignalSource

object FirestoreDataSourceProvider
	: RemoteDataSourceProvider {

	override fun getUserSource(userId: String) = FirestoreUserSource(userId)

	override fun getChatSource(userId: String) = FirestoreChatSourceNoCF.getInstance(userId)

	override fun getMessageSource(chatId: String) = FirestoreMessageNoCF(chatId)

	override fun getContactSource(userId: String) = FirestoreContactSource.getInstance(userId)

	override fun getSignalKeychainSource(userId: String) = FirestoreSignalSource(userId)
}