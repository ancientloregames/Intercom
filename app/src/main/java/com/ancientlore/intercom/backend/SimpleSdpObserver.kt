package com.ancientlore.intercom.backend

import com.ancientlore.intercom.C
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.Logging

open class SimpleSdpObserver : SdpObserver {

	companion object {
		private const val TAG = C.CALLS_LOG_TAG
	}

	override fun onCreateSuccess(sessionDescription: SessionDescription) {
		Logging.d(TAG, "onCreateSuccess: ${sessionDescription.type}")
	}

	override fun onSetSuccess() {
		Logging.d(TAG, "onSetSuccess")
	}

	override fun onCreateFailure(s: String) {
		Logging.d(TAG, "onCreateFailure: $s")
	}

	override fun onSetFailure(s: String) {
		Logging.d(TAG, "onSetFailure: $s")
	}
}