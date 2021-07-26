package com.ancientlore.intercom.crypto.signal

import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import com.ancientlore.intercom.crypto.CryptoUtils
import com.ancientlore.intercom.utils.Utils
import org.whispersystems.libsignal.*
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import java.lang.Exception

class SignalSession(private val localUser: SignalLocalUser,
                    private val remoteUser: SignalRemoteUser) {

	private val sessionCipher: SessionCipher by lazy {
		val protocolStore =
			InMemorySignalProtocolStore(localUser.identityKeyPair, localUser.registrationId)
		for (record in localUser.preKeys) {
			protocolStore.storePreKey(record.id, record)
		}
		protocolStore.storeSignedPreKey(localUser.signedPreKey.id, localUser.signedPreKey)
		val sessionBuilder = SessionBuilder(protocolStore, remoteUser.signalProtocolAddress)
		sessionBuilder.process(remoteUser.preKeyBundle)
		SessionCipher(protocolStore, remoteUser.signalProtocolAddress)
	}

	val remoteUserName get() = remoteUser.remoteUserName

	fun encrypt(text: String): String {
		return try {
			val encryptedBytes = sessionCipher.encrypt(CryptoUtils.toBytes(text))
			val ciphertext = PreKeySignalMessage(encryptedBytes.serialize())
			CryptoUtils.encodeToString(ciphertext.serialize())
		} catch (e: Exception) {
			/*  UntrustedIdentityException, InvalidKeyException,
					InvalidVersionException, InvalidMessageException */
			Utils.logError(e)
			text
		}
	}

	fun decrypt(text: String): String {
		return try {
			val ciphertext = PreKeySignalMessage(CryptoUtils.decode(text))
			val decryptedMessage = sessionCipher.decrypt(ciphertext)
			CryptoUtils.encodeToString(decryptedMessage)
		} catch (e: Exception) {
			/*  UntrustedIdentityException, InvalidKeyException, DuplicateMessageException,
					InvalidMessageException, InvalidKeyIdException, InvalidVersionException, LegacyMessageException */
			Utils.logError(e)
			text
		}
	}
}