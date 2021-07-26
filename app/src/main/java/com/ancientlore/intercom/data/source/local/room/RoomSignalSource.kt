package com.ancientlore.intercom.data.source.local.room

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource

class RoomSignalSource(private val dao: RoomSignalDao): RoomSource(), SignalPrivateKeySource {

	override fun getWorkerThreadName(): String = "roomSignalSource_thread"

	override fun putKeychain(keychain: SignalPrivateKeys, callback: RequestCallback<Any>) {

		exec {
			dao.insert(keychain)
		}
	}

	override fun getKeychain(userId: String, callback: RequestCallback<SignalPrivateKeys>) {

		exec {
			dao.getById(userId)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException)
		}
	}
}