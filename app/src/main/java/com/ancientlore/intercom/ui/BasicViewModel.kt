package com.ancientlore.intercom.ui

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class BasicViewModel : ViewModel() {

	protected val toastRequest: PublishSubject<Int> = PublishSubject.create<Int>() // StringResId

	fun observeToastRequest() = toastRequest as Observable<Int>
}
