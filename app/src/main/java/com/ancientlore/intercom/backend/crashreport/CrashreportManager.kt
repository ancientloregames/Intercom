package com.ancientlore.intercom.backend.crashreport

interface CrashreportManager {

	fun setUserId(userId: String)

	fun report(throwable: Throwable)
}