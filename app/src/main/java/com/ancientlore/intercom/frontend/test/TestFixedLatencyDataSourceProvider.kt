package com.ancientlore.intercom.frontend.test

import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyChatSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyContactSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyMessageSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyUserSource
import com.ancientlore.intercom.frontend.LocalDataSourceProvider

object TestFixedLatencyDataSourceProvider: LocalDataSourceProvider {

	override fun getUserSource(userId: String) = TestFixedLatencyUserSource

	override fun getChatSource(userId: String) = TestFixedLatencyChatSource

	override fun getMessageSource(chatId: String) = TestFixedLatencyMessageSource

	override fun getContactSource(userId: String) = TestFixedLatencyContactSource

	override fun getSignalKeychainSource(userId: String): SignalPrivateKeySource {
		TODO("Not yet implemented")
	}
}