package com.ancientlore.intercom.ui

import com.ancientlore.intercom.widget.recycler.FilterableRecyclerAdapter

abstract class FilterableViewModel<T: FilterableRecyclerAdapter<*, *, *>>(val listAdapter: T)
	: BasicViewModel() {

	fun filter(text: String) {
		listAdapter.filter(text)
	}

	override fun clean() {
		super.clean()
	}
}