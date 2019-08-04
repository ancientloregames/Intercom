package com.ancientlore.intercom.backend

import android.util.Log

abstract class SimpleRequestCallback<DataModel>(private val tag: String = "RequestCallback")
	: RequestCallback<DataModel> {

	override fun onSuccess(result: DataModel) {
		Log.d(tag, result.toString())
	}

	override fun onFailure(error: Throwable) {
		Log.w(tag, error.message ?: "Some error accured during the request")
	}
}