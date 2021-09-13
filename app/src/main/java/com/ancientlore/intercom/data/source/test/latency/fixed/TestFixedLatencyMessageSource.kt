package com.ancientlore.intercom.data.source.test.latency.fixed

import com.ancientlore.intercom.data.source.test.latency.C
import com.ancientlore.intercom.data.source.test.latency.TestLatencyMessageSource
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object TestFixedLatencyMessageSource: TestLatencyMessageSource() {

	private val executor = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "testMessageSource_thread") }

	override fun schedule(command: Runnable) {
		executor.schedule(command, C.AVG_LATENCY_MILLIS, TimeUnit.MILLISECONDS)
	}
}