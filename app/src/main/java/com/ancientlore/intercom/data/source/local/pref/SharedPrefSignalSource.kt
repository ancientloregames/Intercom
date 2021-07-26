package com.ancientlore.intercom.data.source.local.pref

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys
import com.ancientlore.intercom.data.source.local.SignalPrivateKeySource
import java.lang.RuntimeException

class SharedPrefSignalSource(private val context: Context): SignalPrivateKeySource {

	companion object {
		const val KEY_REG_ID = "regId"
		const val KEY_ID_KEY_PAIR = "idKeyPair"
		const val KEY_PRE_KEY_RECS = "preKeyRecs"
		const val KEY_SIGNED_PRE_KEY_REC = "preKeyRecord"
	}

	override fun putKeychain(keychain: SignalPrivateKeys, callback: RequestCallback<Any>) {

		val userId = App.backend.getAuthManager().getCurrentUser().id

		context.getSharedPreferences(userId, AppCompatActivity.MODE_PRIVATE).edit().apply {
			putInt(KEY_REG_ID, keychain.regId)
			putString(KEY_ID_KEY_PAIR, keychain.idKeyPair)
			putString(KEY_PRE_KEY_RECS, keychain.preKeyIds)
			putString(KEY_SIGNED_PRE_KEY_REC, keychain.preKeyRecord)
		}.apply()
	}

	override fun getKeychain(userId: String, callback: RequestCallback<SignalPrivateKeys>) {

		val userPref = context.getSharedPreferences(userId, AppCompatActivity.MODE_PRIVATE)

		val regId = userPref.getInt(KEY_REG_ID, -1)
		val idKeyPair = userPref.getString(KEY_ID_KEY_PAIR, null)
		val preKeyIds = userPref.getString(KEY_PRE_KEY_RECS, null)
		val preKeyRecord = userPref.getString(KEY_SIGNED_PRE_KEY_REC, null)

		if (regId != -1 && idKeyPair != null && preKeyIds != null && preKeyRecord != null) {
			callback.onSuccess(SignalPrivateKeys(regId, idKeyPair, preKeyIds, preKeyRecord, userId))
		}
		else
			callback.onFailure(RuntimeException("SharedPrefSignalSource.getKeychain"))
	}
}