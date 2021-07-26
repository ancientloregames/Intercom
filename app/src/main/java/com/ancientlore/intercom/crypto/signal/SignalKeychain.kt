package com.ancientlore.intercom.crypto.signal

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import com.ancientlore.intercom.crypto.CryptoUtils
import org.json.JSONObject
import org.json.JSONException
import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys
import com.ancientlore.intercom.data.model.crypto.SignalPublicKeys
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.libsignal.util.Medium
import java.util.*

class SignalKeychain(private val identityKeyPair: IdentityKeyPair,
                     private val registrationId: Int,
                     private val preKeys: List<PreKeyRecord>,
                     private val signedPreKeyRecord: SignedPreKeyRecord,
                     private val userId: String) {

	companion object {

		fun generate(userId: String): SignalKeychain {

			val registrationId = KeyHelper.generateRegistrationId(false)
			val identityKeyPair = KeyHelper.generateIdentityKeyPair()
			val preKeys = KeyHelper.generatePreKeys(Random().nextInt(Medium.MAX_VALUE - 101), 100)

			val signedPreKeyId = Random().nextInt(Medium.MAX_VALUE - 1)
			val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId)

			return SignalKeychain(
				identityKeyPair,
				registrationId,
				preKeys,
				signedPreKey,
				userId)
		}
	}

	val privateKeys: SignalPrivateKeys
		get() = SignalPrivateKeys(
			registrationId,
			CryptoUtils.encodeToString(identityKeyPair.serialize()),
			getPrivatePreKeys(),
			CryptoUtils.encodeToString(signedPreKeyRecord.serialize()),
			userId
		)

	val publicKeys: SignalPublicKeys
		get() = SignalPublicKeys(
			registrationId,
			CryptoUtils.encodeToString(identityKeyPair.serialize()),
			getPublicPreKeys(),
			signedPreKeyRecord.id,
			CryptoUtils.encodeToString(signedPreKeyRecord.signature),
			CryptoUtils.encodeToString(signedPreKeyRecord.keyPair.publicKey.serialize())
		)

	private fun getPrivatePreKeys(): String {
		val preKeyList: MutableList<String> = ArrayList()
		for (preKey in preKeys) {
			preKeyList.add(CryptoUtils.encodeToString(preKey.serialize()))
		}
		return Json.encodeToString(ListSerializer(String.serializer()), preKeyList)
	}

	private fun getPublicPreKeys(): List<String> {
		val preKeyList: MutableList<String> = ArrayList()
		try {
			for (preKey in preKeys) {
				val json = JSONObject()
					.putOpt("id", preKey.id)
					.putOpt("publicKey", CryptoUtils.encodeToString(preKey.keyPair.publicKey.serialize()))
				preKeyList.add(json.toString())
			}
		} catch (e: JSONException) {
			e.printStackTrace()
		}
		return preKeyList
	}
}