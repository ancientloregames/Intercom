package com.ancientlore.intercom.backend.test

import com.ancientlore.intercom.backend.RemoteDataSourceProvider
import com.ancientlore.intercom.data.source.remote.SignalPublicKeySource
import com.ancientlore.intercom.data.source.test.instant.TestInstantChatSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantContactSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantMessageSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantUserSource

object TestInstantDataSourceProvider: RemoteDataSourceProvider {

	override fun getUserSource(userId: String) = TestInstantUserSource

	override fun getChatSource(userId: String) = TestInstantChatSource

	override fun getMessageSource(chatId: String) = TestInstantMessageSource

	override fun getContactSource(userId: String) = TestInstantContactSource

	override fun getSignalKeychainSource(userId: String): SignalPublicKeySource {
		TODO("Not yet implemented")
	}
}