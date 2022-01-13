package com.ancientlore.intercom.widget.recycler

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.UiThread
import androidx.databinding.ViewDataBinding

abstract class FilterableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: List<I> = emptyList(),
	withHeader: Boolean = false,
	withFooter: Boolean = false)
	: HeadedRecyclerAdapter<I, H, B>(context, items, withHeader, withFooter), Filterable {

	abstract fun getDiffCallback(newItems: List<I>): HeadedRecyclerDiffUtil.Callback

	protected val fullList = ArrayList<I>(items)

	protected val mutableList get() = getItems() as MutableList<I>

	protected val filter: ListFilter by lazy { createFilter() }

	protected var currentConstraint = ""

	abstract fun createFilter(): ListFilter

	override fun getFilter(): Filter = filter

	fun filter(constraint: String) {
		currentConstraint = constraint
		filter.filter(constraint)
	}

	protected fun getFullListPosition(updatedItem: I) = fullList.indexOfFirst { isTheSame(it, updatedItem) }

	@UiThread
	private fun setFilteredItems(filteredItems: List<I>) {

		HeadedRecyclerDiffUtil
			.calculateDiff(getDiffCallback(filteredItems))
			.dispatchUpdatesTo(this)

		mutableList.clear()
		mutableList.addAll(filteredItems)
	}

	abstract inner class ListFilter: Filter() {

		abstract fun satisfy(item: I, candidate: String): Boolean

		override fun performFiltering(constraint: CharSequence?): FilterResults {
			val candidate = constraint?.toString()?.lowercase() ?: ""
			val resultList =
				if (candidate.isNotEmpty())
					fullList.filter { satisfy(it, candidate) }
				else fullList

			return FilterResults().apply {
				count = resultList.size
				values = resultList
			}
		}

		override fun publishResults(constraint: CharSequence?, results: FilterResults) {
			results.values?.let {
				setFilteredItems(it as List<I>)
			}
		}
	}
}