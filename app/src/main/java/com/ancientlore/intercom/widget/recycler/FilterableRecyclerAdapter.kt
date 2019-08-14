package com.ancientlore.intercom.widget.recycler

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.ViewDataBinding

abstract class FilterableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: MutableList<I>)
	: MutableRecyclerAdapter<I, H, B>(context, items), Filterable {

	protected val fullList = mutableListOf<I>()

	private val filter: ListFilter by lazy { createFilter() }

	private var currentConstraint = ""

	abstract fun createFilter(): ListFilter

	override fun getFilter(): Filter = filter

	override fun setItems(newItems: List<I>) {
		fullList.clear()
		fullList.addAll(newItems)
		super.setItems(newItems)
	}

	override fun prependItem(newItem: I): Boolean {
		if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint))
			super.prependItem(newItem)

		if (isUnique(newItem)) {
			fullList.add(0, newItem)
			return true
		}
		return false
	}

	override fun appendItem(newItem: I): Boolean {
		if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint))
			super.appendItem(newItem)

		if (isUnique(newItem)) {
			fullList.add(newItem)
			return true
		}
		return false
	}

	override fun updateItem(updatedItem: I): Boolean {
		val position = getFullListPosition(updatedItem)

		if (position != -1) fullList[position] = updatedItem

		return super.updateItem(updatedItem)
	}

	override fun deleteItem(itemToDelete: I): Boolean {
		val position = getFullListPosition(itemToDelete)

		if (position != -1) deleteItemAt(position)

		return super.deleteItem(itemToDelete)
	}

	fun filter(constraint: String) {
		currentConstraint = constraint
		filter.filter(constraint)
	}

	private fun getFullListPosition(updatedItem: I) = fullList.indexOfFirst { isTheSame(it, updatedItem) }

	private fun setFilteredItems(filteredItems: List<I>) {
		resetItems(filteredItems)
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
				setFilteredItems(it as MutableList<I>)
				notifyDataSetChanged()
			}
		}
	}
}