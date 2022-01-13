package com.ancientlore.intercom.widget.recycler

import android.content.Context
import androidx.annotation.UiThread
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.widget.MutableAdapter
import java.util.*

abstract class MutableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: MutableList<I> = mutableListOf(),
	withHeader: Boolean = false,
	withFooter: Boolean = false,
	protected var autoSort: Boolean = false)
	: FilterableRecyclerAdapter<I, H, B>(context, items, withHeader, withFooter),
	MutableAdapter<I> {

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
			notifyListItemInserted(position)
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
	override fun prependItems(newItems: List<I>): Boolean {

		if (newItems.isEmpty())
			return true

		val items =
			if (autoSort) newItems.sorted()
			else newItems

		// TODO maybe should check uniqueness
		fullList.addAll(0, items)

		if (currentConstraint.isEmpty()) {
			mutableList.addAll(0, items)
			notifyListItemRangeInserted(0, items.size)
			return true
		}
		else {
			val setisfactoryNewItems = LinkedList<I>().apply {
				for (candidate in items) {
					if (filter.satisfy(candidate, currentConstraint))
						add(candidate)
				}
			}
			if (setisfactoryNewItems.isNotEmpty()) {
				mutableList.addAll(0, setisfactoryNewItems)
				notifyListItemRangeInserted(0, setisfactoryNewItems.size)
				return true
			}
		}

		return false
	}

	@UiThread
	override fun appendItem(newItem: I): Boolean {
		if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint)) {
			if (isUnique(newItem)) {
				mutableList.add(newItem)
				notifyListItemInserted(getLastItemPosition())
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
	override fun appendItems(newItems: List<I>): Boolean {
		// TODO maybe should check uniqueness
		fullList.addAll(newItems)

		val startPos = itemCount
		if (currentConstraint.isEmpty()) {
			mutableList.addAll(newItems)
			notifyListItemRangeInserted(startPos, getLastItemPosition())
			return true
		}
		else {
			val setisfactoryNewItems = LinkedList<I>().apply {
				for (candidate in newItems) {
					if (filter.satisfy(candidate, currentConstraint))
						add(candidate)
				}
			}
			if (setisfactoryNewItems.isNotEmpty()) {
				mutableList.addAll(setisfactoryNewItems)
				notifyListItemRangeInserted(startPos, getLastItemPosition())
				return true
			}
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