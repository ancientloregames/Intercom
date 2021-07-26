package com.ancientlore.intercom.backend

import com.ancientlore.intercom.data.source.remote.SignalPublicKeySource

interface RemoteDataSourceProvider: DataSourceProvider {

	fun getSignalKeychainSource(userId: String): SignalPublicKeySource
}