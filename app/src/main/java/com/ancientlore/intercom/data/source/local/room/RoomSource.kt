package com.ancientlore.intercom.data.source.local.room

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class RoomSource {

	private val service: ExecutorService = Executors.newSingleThreadExecutor { r -> Thread(r, getWorkerThreadName()) }

	protected abstract fun getWorkerThreadName(): String

	protected fun exec(command: Runnable) {
		service.execute(command)
	}
}