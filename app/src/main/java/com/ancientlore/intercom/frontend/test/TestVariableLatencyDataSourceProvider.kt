package com.ancientlore.intercom.frontend.test

import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyChatSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyContactSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyMessageSource
import com.ancientlore.intercom.data.source.test.latency.variable.TestVariableLatencyUserSource
import com.ancientlore.intercom.frontend.LocalDataSourceProvider

object TestVariableLatencyDataSourceProvider: LocalDataSourceProvider {

	override fun getUserSource(userId: String) = TestVariableLatencyUserSource

	override fun getChatSource(userId: String) = TestVariableLatencyChatSource

	override fun getMessageSource(chatId: String) = TestVariableLatencyMessageSource

	override fun getContactSource(userId: String) = TestVariableLatencyContactSource

	override fun getSignalKeychainSource(userId: String): SignalPrivateKeySource {
		TODO("Not yet implemented")
	}
}