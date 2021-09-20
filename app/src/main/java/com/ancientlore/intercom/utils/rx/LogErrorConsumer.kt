package com.ancientlore.intercom.utils.rx

import androidx.annotation.CallSuper
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.Utils
import io.reactivex.functions.Consumer

open class LogErrorConsumer<T: Throwable>: Consumer<T> {

	@CallSuper
	override fun accept(t: T) {
		if (t !is EmptyResultException)
			Utils.logError(t)
	}
}