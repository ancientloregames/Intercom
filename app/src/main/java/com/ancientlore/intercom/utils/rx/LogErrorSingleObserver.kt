package com.ancientlore.intercom.utils.rx

import com.ancientlore.intercom.utils.Utils
import io.reactivex.disposables.Disposable
import io.reactivex.SingleObserver
import io.reactivex.functions.Consumer
import io.reactivex.observers.LambdaConsumerIntrospection
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.functions.Functions
import java.util.concurrent.atomic.AtomicReference

class LogErrorSingleObserver<T>(private val onSuccess: Consumer<in T>,
                                private val onError: Consumer<in Throwable>? = null)
	: AtomicReference<Disposable?>(), SingleObserver<T>, Disposable, LambdaConsumerIntrospection {

	override fun onError(e: Throwable) {

		lazySet(DisposableHelper.DISPOSED)
		Utils.logError(e)

		onError?.run {
			try {
				accept(e)
			} catch (ex: Throwable) {
				Utils.logError(ex)
			}
		}
	}

	override fun onSubscribe(d: Disposable) {
		DisposableHelper.setOnce(this, d)
	}

	override fun onSuccess(value: T) {
		lazySet(DisposableHelper.DISPOSED)
		try {
			onSuccess.accept(value)
		} catch (ex: Throwable) {
			Utils.logError(ex)
		}
	}

	override fun dispose() {
		DisposableHelper.dispose(this)
	}

	override fun isDisposed(): Boolean {
		return get() === DisposableHelper.DISPOSED
	}

	override fun hasCustomOnError(): Boolean {
		return onError !== Functions.ON_ERROR_MISSING
	}
}