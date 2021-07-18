package com.ancientlore.intercom.frontend

import android.content.Context
import com.ancientlore.intercom.crypto.CryptoManager
import com.ancientlore.intercom.crypto.symmetric.AesCryptoManager

class IntercomFrontendFactory(private val appContext: Context) : FrontendFactory {

	override fun getDataSourceProvider() = RoomDataSourceProvider(appContext)

	override fun getCryptoManager(userId: String): CryptoManager = AesCryptoManager
}