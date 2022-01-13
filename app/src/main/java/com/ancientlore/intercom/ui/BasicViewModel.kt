package com.ancientlore.intercom.ui

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

abstract class BasicViewModel : ViewModel() {

	private val subscriptions: CompositeDisposable = CompositeDisposable()

	protected val toastRequest: PublishSubject<Int> = PublishSubject.create()

	fun observeToastRequest() = toastRequest as Observable<Int>

	@CallSuper
	open fun clean() {
		subscriptions.clear()
		toastRequest.onComplete()
	}

	protected fun subscribe(vararg disposables: Disposable) {
		subscriptions.addAll(*disposables)
	}
}
