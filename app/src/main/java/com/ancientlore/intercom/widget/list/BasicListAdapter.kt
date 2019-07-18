package com.ancientlore.intercom.widget.list

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class BasicListAdapter<T>(context: Context) : BaseAdapter() {

	protected val inflater: LayoutInflater = LayoutInflater.from(context)

	private val items = mutableListOf<T>()

	protected val itemClickedEvent: Subject<T> = PublishSubject.create<T>()

	override fun getCount() = items.size

	override fun getItem(position: Int) = items[position]

	override fun getItemId(position: Int) = position.toLong()

	@UiThread
	@CallSuper
	open fun setItems(items: List<T>) {
		this.items.clear()
		this.items.addAll(items)
		notifyDataSetChanged()
	}

	fun observeItemClicked() = itemClickedEvent as Observable<T>
}