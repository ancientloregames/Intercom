package com.ancientlore.intercom.backend

import com.ancientlore.intercom.utils.Utils

abstract class CrashlyticsRequestCallback<DataModel>
	: RequestCallback<DataModel> {

	override fun onFailure(error: Throwable) {
		Utils.logError(error)
	}
}