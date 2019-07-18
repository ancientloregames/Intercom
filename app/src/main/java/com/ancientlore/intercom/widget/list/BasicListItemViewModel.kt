package com.ancientlore.intercom.widget.list

import androidx.databinding.BaseObservable

abstract class BasicListItemViewModel<T>(val item: T) : BaseObservable() {

	interface Listener<T> {
		fun onItemClick(item: T)
	}
	private var listener: Listener<T>? = null

	fun onClick() = listener?.onItemClick(item)

	fun setListener(listener: Listener<T>) { this.listener = listener }
}