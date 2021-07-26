package com.ancientlore.intercom.crypto.signal

import org.whispersystems.libsignal.SignalProtocolAddress

abstract class SignalUser protected constructor(
	val registrationId: Int,
	val signalProtocolAddress: SignalProtocolAddress) {

	val remoteUserName: String
		get() = signalProtocolAddress.name
}