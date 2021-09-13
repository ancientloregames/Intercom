package com.ancientlore.intercom.backend.test

import com.ancientlore.intercom.backend.RemoteDataSourceProvider
import com.ancientlore.intercom.data.source.remote.SignalPublicKeySource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyChatSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyContactSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyMessageSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyUserSource

object TestVariableLatencyDataSourceProvider: RemoteDataSourceProvider {

	override fun getUserSource(userId: String) = TestVariableLatencyUserSource

	override fun getChatSource(userId: String) = TestVariableLatencyChatSource

	override fun getMessageSource(chatId: String) = TestVariableLatencyMessageSource

	override fun getContactSource(userId: String) = TestVariableLatencyContactSource

	override fun getSignalKeychainSource(userId: String): SignalPublicKeySource {
		TODO("Not yet implemented")
	}
}