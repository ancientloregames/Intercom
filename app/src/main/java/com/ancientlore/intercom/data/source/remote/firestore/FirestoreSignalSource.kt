package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.crypto.SignalPublicKeys
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.remote.SignalPublicKeySource

class FirestoreSignalSource(private val userId: String)
	: FirestoreSource<SignalPublicKeys>(), SignalPublicKeySource {

	private val userKeys get() = db.collection(C.CRYPTOS).document(userId)

	override fun equals(other: Any?): Boolean {
		return other is FirestoreSignalSource && other.userId == userId
	}

	override fun getObjectClass() = SignalPublicKeys::class.java

	override fun getWorkerThreadName() = "fsCryptoSource_thread"

	override fun putKeychain(keychain: SignalPublicKeys, callback: RequestCallback<Any>) {

		userKeys.set(keychain)
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getKeychain(userId: String, callback: RequestCallback<SignalPublicKeys>) {

		db.collection(C.CRYPTOS)
			.document(userId)
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					if (snapshot.exists()) {
						deserialize(snapshot)
							?.let { callback.onSuccess(it) }
							?: callback.onFailure(EmptyResultException)
					}
					else callback.onFailure(EmptyResultException)
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}
}