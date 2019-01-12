package com.ancientlore.intercom.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BasicRecyclerAdapter<I, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	private val items: List<I>)
	: RecyclerView.Adapter<H>() {

	companion object {
		private const val VIEW_TYPE_ITEM = 0
	}

	protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

	abstract fun createItemViewDataBinding(parent: ViewGroup): B

	abstract fun getViewHolder(binding: B): H

	override fun getItemCount() = items.count()

	override fun getItemViewType(position: Int) = VIEW_TYPE_ITEM

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
		val binding = createItemViewDataBinding(parent)
		return getViewHolder(binding)
	}

	override fun onBindViewHolder(holder: H, position: Int) = holder.bind(items[position])

	fun isEmpty() = items.isEmpty()

	protected fun getItems() = items

	protected fun isValidPosition(position: Int) = position > -1 && position < items.size

	protected fun getItemPosition(updatedItem: I) = items.indexOfFirst { isTheSame(it, updatedItem) }

	protected open fun isTheSame(first: I, second: I) = first == second

	protected open fun isUnique(item: I) = items.none { it == item }

	abstract class ViewHolder<T, B: ViewDataBinding>(protected val binding: B) : RecyclerView.ViewHolder(binding.root), Bindable<T>
}