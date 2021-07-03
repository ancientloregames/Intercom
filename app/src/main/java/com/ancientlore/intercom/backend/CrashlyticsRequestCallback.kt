package com.ancientlore.intercom.backend

import androidx.annotation.CallSuper
import com.ancientlore.intercom.C.DEFAULT_LOG_TAG
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.Utils

abstract class CrashlyticsRequestCallback<DataModel>(private val tag: String = DEFAULT_LOG_TAG)
	: RequestCallback<DataModel> {

	override fun onSuccess(result: DataModel) {}

	@CallSuper
	override fun onFailure(error: Throwable) {
		if (error !is EmptyResultException)
			Utils.logError(error, tag)
	}
}