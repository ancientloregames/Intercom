package com.ancientlore.intercom.crypto.signal

import com.ancientlore.intercom.data.model.crypto.SignalPublicKeys
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyBundle
import java.util.*

class SignalRemoteUser(name: String,
                       keychain: SignalPublicKeys)
	: SignalUser(keychain.regId, SignalProtocolAddress(name, 2)) {

	private val preKeyId: Int
	private val preKeyPublicKey: ECPublicKey
	private val signedPreKeyId: Int
	private val signedPreKeyPublicKey: ECPublicKey
	private val signedPreKeySignature: ByteArray
	private val identityKeyPairPublicKey: IdentityKey

	init {

		// FIXME this is a simulation. Real encryption suppose receiving a key picked randomly on server
		val index = Random().nextInt() % keychain.preKeys.size
		val randomPreKey = Json.decodeFromString<PublicPreKey>(keychain.preKeys[index])

		this.preKeyId = randomPreKey.id
		this.preKeyPublicKey = Curve.decodePoint(randomPreKey.publicKey.toByteArray(), 0)
		this.signedPreKeyId = keychain.signedId
		this.signedPreKeyPublicKey = Curve.decodePoint(keychain.signedPublicKey.toByteArray(), 0)
		this.signedPreKeySignature = keychain.signedSignature.toByteArray()
		this.identityKeyPairPublicKey = IdentityKey(keychain.idKeyPair.toByteArray(), 0)
	}

	val preKeyBundle: PreKeyBundle
		get() = PreKeyBundle(
			registrationId,
			signalProtocolAddress.deviceId,
			preKeyId,
			preKeyPublicKey,
			signedPreKeyId,
			signedPreKeyPublicKey,
			signedPreKeySignature,
			identityKeyPairPublicKey
		)

	@Serializable
	data class PublicPreKey(val id: Int,
	                        val publicKey: String)
}