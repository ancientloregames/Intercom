package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.BackendFactory
import com.ancientlore.intercom.backend.crashreport.FirebaseCrashreportManager

object FirebaseFactory : BackendFactory() {

	override fun getAuthManager() = FirebaseAuthManager

	override fun getDataSourceProvider() = FirestoreDataSourceProvider

	override fun getStorageManager() = FirebaseStorageManager

	override fun getMessagingManager() = FirebaseMessagingManager

	override fun getCallManager() = FirestoreWebrtcCallManager

	override fun getCrashreportManager() = FirebaseCrashreportManager
}