package com.ancientlore.intercom.frontend

import com.ancientlore.intercom.backend.DataSourceProvider
import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource

interface LocalDataSourceProvider: DataSourceProvider {

	fun getSignalKeychainSource(userId: String): SignalPrivateKeySource
}