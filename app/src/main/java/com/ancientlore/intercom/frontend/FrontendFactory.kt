package com.ancientlore.intercom.frontend

import com.ancientlore.intercom.backend.DataSourceProvider
import com.ancientlore.intercom.crypto.CryptoManager

interface FrontendFactory {

	fun getDataSourceProvider(): DataSourceProvider

	fun getCryptoManager(userId: String): CryptoManager
}