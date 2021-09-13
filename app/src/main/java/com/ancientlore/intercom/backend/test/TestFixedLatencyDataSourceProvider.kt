package com.ancientlore.intercom.backend.test

import com.ancientlore.intercom.backend.RemoteDataSourceProvider
import com.ancientlore.intercom.data.source.remote.SignalPublicKeySource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyChatSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyContactSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyMessageSource
import com.ancientlore.intercom.data.source.test.latency.fixed.TestFixedLatencyUserSource

object TestFixedLatencyDataSourceProvider: RemoteDataSourceProvider {

		override fun getUserSource(userId: String) = TestFixedLatencyUserSource

		override fun getChatSource(userId: String) = TestFixedLatencyChatSource

		override fun getMessageSource(chatId: String) = TestFixedLatencyMessageSource

		override fun getContactSource(userId: String) = TestFixedLatencyContactSource

		override fun getSignalKeychainSource(userId: String): SignalPublicKeySource {
				TODO("Not yet implemented")
		}
}