package com.ancientlore.intercom.widget.list

import android.content.Context
import android.widget.ListAdapter
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

abstract class BasicListViewModel<T>(context: Context) : BaseObservable() {

	private val adapter = createListAdapter(context)

	protected abstract fun createListAdapter(context: Context): BasicListAdapter<T>

	fun setAdapterItems(items: List<T>) = adapter.setItems(items)

	@Bindable
	fun getListAdapter() = adapter as ListAdapter

	fun observeItemClicked() = adapter.observeItemClicked()
}