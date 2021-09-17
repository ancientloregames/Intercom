package com.ancientlore.intercom.utils

import java.util.concurrent.ThreadFactory

class LoggingThreadFactory(private val threadName: String): ThreadFactory {

	override fun newThread(r: Runnable?): Thread {

		return object : Thread(r, threadName) {
			override fun run() {
				try {
					super.run()
				} catch (e: Throwable) {
					Utils.logError(e)
				}
			}
		}
	}
}