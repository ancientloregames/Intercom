package com.ancientlore.intercom.frontend.test

import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource
import com.ancientlore.intercom.data.source.test.instant.TestInstantChatSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantContactSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantMessageSource
import com.ancientlore.intercom.data.source.test.instant.TestInstantUserSource
import com.ancientlore.intercom.frontend.LocalDataSourceProvider

object TestInstantDataSourceProvider: LocalDataSourceProvider {

	override fun getUserSource(userId: String) = TestInstantUserSource

	override fun getChatSource(userId: String) = TestInstantChatSource

	override fun getMessageSource(chatId: String) = TestInstantMessageSource

	override fun getContactSource(userId: String) = TestInstantContactSource

	override fun getSignalKeychainSource(userId: String): SignalPrivateKeySource {
		TODO("Not yet implemented")
	}
}