package com.ancientlore.intercom.backend

import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.crashreport.CrashreportManager

abstract class BackendFactory {
	abstract fun getAuthManager(): AuthManager
	abstract fun getDataSourceProvider(): RemoteDataSourceProvider
	abstract fun getStorageManager(): StorageManager
	abstract fun getMessagingManager(): MessagingManager
	abstract fun getCallManager(): CallManager<*>
	abstract fun getCrashreportManager(): CrashreportManager
}