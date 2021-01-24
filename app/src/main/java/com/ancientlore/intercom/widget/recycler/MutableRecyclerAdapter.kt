package com.ancientlore.intercom.widget.recycler

import android.content.Context
import androidx.annotation.UiThread
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.widget.MutableAdapter

abstract class MutableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: MutableList<I>)
	: BasicRecyclerAdapter<I, H, B>(context, items),
	MutableAdapter<I> {

	protected val mutableList get() = getItems() as MutableList<I>

	protected var autoSort = false

	abstract fun getDiffCallback(newItems: List<I>): DiffUtil.Callback

	@UiThread
	override fun setItems(newItems: List<I>) {
		val items =
			if (autoSort) newItems.sorted()
			else newItems

		val diffResult = DiffUtil.calculateDiff(getDiffCallback(items))
		diffResult.dispatchUpdatesTo(this)

		resetItems(items)
	}

	protected fun resetItems(newItems: List<I>) {
		mutableList.clear()
		mutableList.addAll(newItems)
	}

	@UiThread
	override fun setItem(newItem: I, position: Int): Boolean {
		if (isUnique(newItem) && isValidPosition(position)) {
			mutableList.add(position, newItem)
			notifyItemInserted(itemCount - 1)
			return true
		}

		return false
	}

	@UiThread
	override fun prependItem(newItem: I): Boolean {
		if (isUnique(newItem)) {
			mutableList.add(0, newItem)
			notifyItemInserted(0)
			return true
		}

		return false
	}

	@UiThread
	override fun appendItem(newItem: I): Boolean {
		if (isUnique(newItem)) {
			mutableList.add(newItem)
			notifyItemInserted(itemCount - 1)
			return true
		}

		return false
	}

	@UiThread
	override fun updateItem(updatedItem: I) = updateItemAt(getItemPosition(updatedItem), updatedItem)

	@UiThread
	override fun deleteItem(itemToDelete: I) = deleteItemAt(getItemPosition(itemToDelete))

	@UiThread
	private fun updateItemAt(position: Int, updatedItem: I): Boolean {
		if (isValidPosition(position)) {
			mutableList[position] = updatedItem
			notifyItemChanged(position)
			return true
		}

		return false
	}

	@UiThread
	protected fun deleteItemAt(position: Int): Boolean {
		if (isValidPosition(position)) {
			mutableList.removeAt(position)
			notifyItemRemoved(position)
			return true
		}

		return false
	}
}