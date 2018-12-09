package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.BackendFactory

object FirebaseFactory : BackendFactory() {

	override fun getAuthManager() = FirebaseAuthManager
}