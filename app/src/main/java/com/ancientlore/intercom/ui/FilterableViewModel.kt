package com.ancientlore.intercom.ui

import com.ancientlore.intercom.widget.recycler.FilterableRecyclerAdapter

abstract class FilterableViewModel<T: FilterableRecyclerAdapter<*, *, *>>
	: BasicViewModel() {

	abstract fun getListAdapter(): T

	fun filter(text: String) {
		getListAdapter().filter(text)
	}

	override fun clean() {
		super.clean()
	}
}