package com.ancientlore.intercom.backend.crashreport

import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseCrashreportManager: CrashreportManager {

	override fun setUserId(userId: String) {
		FirebaseCrashlytics.getInstance().setUserId(userId)
	}

	override fun report(throwable: Throwable) {
		FirebaseCrashlytics.getInstance().recordException(throwable)
	}
}