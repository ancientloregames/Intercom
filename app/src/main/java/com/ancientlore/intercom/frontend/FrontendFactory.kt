package com.ancientlore.intercom.frontend

import com.ancientlore.intercom.crypto.CryptoManager

interface FrontendFactory {

	fun getDataSourceProvider(): LocalDataSourceProvider

	fun getCryptoManager(userId: String): CryptoManager
}