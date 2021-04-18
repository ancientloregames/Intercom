package com.ancientlore.intercom.widget.recycler

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.ViewDataBinding

abstract class FilterableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: List<I>)
	: BasicRecyclerAdapter<I, H, B>(context, items), Filterable {


	protected val fullList = mutableListOf<I>()

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

	private fun setFilteredItems(filteredItems: List<I>) {
		mutableList.clear()
		mutableList.addAll(filteredItems)
	}

	abstract inner class ListFilter: Filter() {

		abstract fun satisfy(item: I, candidate: String): Boolean

		override fun performFiltering(constraint: CharSequence?): FilterResults {
			val candidate = constraint?.toString()?.toLowerCase() ?: ""
			val resultList =
				if (candidate.isNotEmpty())
					fullList.filter { satisfy(it, candidate) }
				else fullList

			val result = FilterResults()
			result.count = resultList.size
			result.values = resultList

			return result
		}

		override fun publishResults(constraint: CharSequence?, results: FilterResults) {
			results.values?.let {
				setFilteredItems(it as List<I>)
				notifyDataSetChanged()
			}
		}
	}
}