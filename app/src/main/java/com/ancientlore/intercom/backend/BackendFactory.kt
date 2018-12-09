package com.ancientlore.intercom.backend

import com.ancientlore.intercom.backend.auth.AuthManager

abstract class BackendFactory {
	abstract fun getAuthManager(): AuthManager
}