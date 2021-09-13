package com.ancientlore.intercom.data.source.test.latency.variable

import com.ancientlore.intercom.data.source.test.latency.C
import com.ancientlore.intercom.data.source.test.latency.TestLatencyContactSource
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object TestVariableLatencyContactSource: TestLatencyContactSource() {

	private val random = Random()

	private val executor = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "testContactSource_thread") }

	override fun schedule(command: Runnable) {
		executor.schedule(command,
			random.nextInt(C.LATENCY_RANDOM_BOUND_MILLIS).toLong() + C.MIN_LATENCY_MILLIS,
			TimeUnit.MILLISECONDS)
	}
}