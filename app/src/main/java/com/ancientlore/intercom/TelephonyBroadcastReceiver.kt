package com.ancientlore.intercom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.annotation.IntDef
import java.util.*
import kotlin.collections.LinkedHashSet

class TelephonyBroadcastReceiver: BroadcastReceiver() {

	companion object {
		const val ACTION_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL"
		const val ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE"

		const val STATE_IDLE = 0
		const val STATE_INCALL_RINGING = 1
		const val STATE_INCALL_STARTED = 2
		const val STATE_OUTCALL_RINGING = 3
		const val STATE_OUTCALL_STARTED = 4
		const val STATE_CALL_ENDED = 5
		const val STATE_CALL_MISSED = 6

	}

	@IntDef(STATE_IDLE, STATE_INCALL_RINGING, STATE_OUTCALL_STARTED)
	@Retention(AnnotationRetention.SOURCE)
	annotation class TelephonyState

	interface Listener {
		fun onTelephonyStateChange(@TelephonyState state: Int)
	}

	private val listeners = Collections.synchronizedSet(LinkedHashSet<Listener>())

	private val lock = Any()

	private var lastState = TelephonyManager.EXTRA_STATE_IDLE

	override fun onReceive(context: Context?, intent: Intent?) {

		intent?.run {
			if (action == ACTION_OUTGOING_CALL)
				notifyListeners(STATE_OUTCALL_RINGING)
			else if (action == ACTION_PHONE_STATE)
				handleIncommingCall(
					getStringExtra(TelephonyManager.EXTRA_STATE))
		}
	}

	private fun handleIncommingCall(state: String) {
		if (lastState == state)
			return

		val newInnerState = when (state) {
			TelephonyManager.EXTRA_STATE_RINGING -> STATE_INCALL_RINGING
			TelephonyManager.EXTRA_STATE_OFFHOOK -> {
				if (lastState == TelephonyManager.EXTRA_STATE_RINGING)
					STATE_INCALL_STARTED
				else
					STATE_OUTCALL_STARTED
			}
			else -> {
				if (lastState == TelephonyManager.EXTRA_STATE_RINGING)
					STATE_CALL_MISSED
				else
					STATE_CALL_ENDED
			}
		}
		lastState = state

		notifyListeners(newInnerState)
	}

	private fun notifyListeners(@TelephonyState newState: Int) {
		synchronized(lock) {
			for (listener in listeners)
				listener.onTelephonyStateChange(newState)
		}
	}

	fun addListener(listener: Listener) {
		listeners.add(listener)
	}

	fun removeListener(listener: Listener) {
		listeners.remove(listener)
	}

	fun removeListeners() {
		listeners.clear()
	}
}