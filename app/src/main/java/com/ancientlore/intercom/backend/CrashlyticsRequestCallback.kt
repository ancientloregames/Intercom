package com.ancientlore.intercom.backend

import androidx.annotation.CallSuper
import com.ancientlore.intercom.utils.Utils

abstract class CrashlyticsRequestCallback<DataModel>
	: RequestCallback<DataModel> {

	@CallSuper
	override fun onFailure(error: Throwable) {
		Utils.logError(error)
	}
}