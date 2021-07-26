package com.ancientlore.intercom.crypto.signal

import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord

class SignalLocalUser(name: String,
                      keychain: SignalPrivateKeys)
	: SignalUser(keychain.regId, SignalProtocolAddress(name, 1)) {

	val identityKeyPair: IdentityKeyPair = IdentityKeyPair(keychain.idKeyPair.toByteArray())
	val preKeys: List<PreKeyRecord> = Json.decodeFromString(ListSerializer(String.serializer()), keychain.preKeyIds)
		.map { PreKeyRecord(it.toByteArray()) }
	val signedPreKey: SignedPreKeyRecord = SignedPreKeyRecord(keychain.preKeyRecord.toByteArray())
}