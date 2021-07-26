package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny

interface KeychainSource<T> {

	fun putKeychain(keychain: T, callback: RequestCallback<Any> = RequestCallbackAny)

	fun getKeychain(userId: String, callback: RequestCallback<T>)
}