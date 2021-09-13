package com.ancientlore.intercom.data.source.test.latency

interface LatencySource {
	fun schedule(command: Runnable)
}

object C {
	const val MIN_LATENCY_MILLIS = 100L
	const val AVG_LATENCY_MILLIS = 1000L
	const val MAX_LATENCY_MILLIS = 4000L
	const val LATENCY_RANDOM_BOUND_MILLIS = (MAX_LATENCY_MILLIS - MIN_LATENCY_MILLIS).toInt()
}