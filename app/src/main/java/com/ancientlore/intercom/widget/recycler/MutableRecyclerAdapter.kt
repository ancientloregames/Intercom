package com.ancientlore.intercom.widget.recycler

import android.content.Context
import androidx.annotation.UiThread
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.widget.MutableAdapter

abstract class MutableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: MutableList<I> = mutableListOf(),
	withHeader: Boolean = false,
	withFooter: Boolean = false,
	private var autoSort: Boolean = false)
	: FilterableRecyclerAdapter<I, H, B>(context, items, withHeader, withFooter),
	MutableAdapter<I> {

	abstract fun getDiffCallback(newItems: List<I>): HeadedRecyclerDiffUtil.Callback

	@UiThread
	override fun setItems(newItems: List<I>) {

		val items =
			if (autoSort) newItems.sorted()
			else newItems

		fullList.clear()
		fullList.addAll(items)

		val diffResult = HeadedRecyclerDiffUtil.calculateDiff(getDiffCallback(items))
		diffResult.dispatchUpdatesTo(this)

		mutableList.clear()
		mutableList.addAll(items)
	}

	@UiThread
	override fun setItem(newItem: I, position: Int): Boolean {
		if (isUnique(newItem) && isValidPosition(position)) {
			mutableList.add(position, newItem)
			notifyListItemInserted(itemCount - 1)
			return true
		}

		return false
	}

	@UiThread
	override fun prependItem(newItem: I): Boolean {
		if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint)) {
			if (isUnique(newItem)) {
				mutableList.add(0, newItem)
				notifyListItemInserted(0)
				return true
			}
		}

		if (isUnique(newItem)) {
			fullList.add(0, newItem)
			return true
		}
		return false
	}

	@UiThread
	override fun appendItem(newItem: I): Boolean {
		if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint)) {
			if (isUnique(newItem)) {
				mutableList.add(newItem)
				notifyListItemInserted(itemCount - 1)
				return true
			}
		}

		if (isUnique(newItem)) {
			fullList.add(newItem)
			return true
		}

		return false
	}

	@UiThread
	override fun updateItem(updatedItem: I) : Boolean {

		val position = getFullListPosition(updatedItem)

		if (position != -1) fullList[position] = updatedItem

		return updateItemAt(getItemPosition(updatedItem), updatedItem)
	}

	@UiThread
	override fun deleteItem(itemToDelete: I) : Boolean {
		val position = getFullListPosition(itemToDelete)

		if (position != -1) deleteItemAt(position)

		return deleteItemAt(getItemPosition(itemToDelete))
	}

	@UiThread
	private fun updateItemAt(position: Int, updatedItem: I): Boolean {
		if (isValidPosition(position)) {
			mutableList[position] = updatedItem
			notifyListItemChanged(position)
			return true
		}

		return false
	}

	@UiThread
	protected fun deleteItemAt(position: Int): Boolean {
		if (isValidPosition(position)) {
			mutableList.removeAt(position)
			notifyListItemRemoved(position)
			return true
		}

		return false
	}
}