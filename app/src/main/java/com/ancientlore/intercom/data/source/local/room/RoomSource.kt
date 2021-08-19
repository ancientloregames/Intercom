package com.ancientlore.intercom.data.source.local.room

import com.ancientlore.intercom.utils.Utils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class RoomSource {

	private val service: ExecutorService = Executors.newSingleThreadExecutor { r ->
		object : Thread(r, getWorkerThreadName()) {
			override fun run() {
				try {
					super.run()
				} catch (e: Throwable) {
					Utils.logError(e)
				}
			}
		}
	}

	protected abstract fun getWorkerThreadName(): String

	protected fun exec(command: Runnable) {
		service.execute(command)
	}
}