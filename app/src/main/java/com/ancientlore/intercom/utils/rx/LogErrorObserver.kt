package com.ancientlore.intercom.utils.rx

import android.util.Log
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class LogErrorObserver<T>: Observer<T> {

	override fun onSubscribe(d: Disposable) {
		Log.d("Intercom", "onSubscribe")
	}

	override fun onComplete() {
		Log.d("Intercom", "onComplete")
	}

	override fun onError(e: Throwable) {
		Log.d("Intercom", "onError")
		if (e !is EmptyResultException)
			Utils.logError(e)
	}
}