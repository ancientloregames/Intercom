package com.ancientlore.intercom.backend

import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.data.DataSourceProvider

abstract class BackendFactory {
	abstract fun getAuthManager(): AuthManager
	abstract fun getDataSourceProvider(userId: String): DataSourceProvider
}