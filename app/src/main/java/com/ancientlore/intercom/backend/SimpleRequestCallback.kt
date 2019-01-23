package com.ancientlore.intercom.backend

import android.util.Log

abstract class SimpleRequestCallback<DataModel>
	: RequestCallback<DataModel> {

	override fun onFailure(error: Throwable) {
		Log.w("DataSource", error.message ?: "Some error accured during the request")
	}
}