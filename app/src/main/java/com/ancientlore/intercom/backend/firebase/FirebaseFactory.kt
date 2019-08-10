package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.BackendFactory

object FirebaseFactory : BackendFactory() {

	override fun getAuthManager() = FirebaseAuthManager

	override fun getDataSourceProvider() = FirestoreDataSourceProvider

	override fun getStorageManager() = FirebaseStorageManager

	override fun getMessagingManager() = FirebaseMessagingManager
}