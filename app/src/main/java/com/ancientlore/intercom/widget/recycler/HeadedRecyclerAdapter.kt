package com.ancientlore.intercom.widget.recycler

import android.content.Context
import android.os.Bundle
import androidx.annotation.UiThread
import androidx.databinding.ViewDataBinding

abstract class HeadedRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	context: Context,
	items: List<I> = emptyList(),
	private val withHeader: Boolean = false,
	private val withFooter: Boolean = false)
	: BasicRecyclerAdapter<I, H, B>(context, items) {

	companion object {
		const val VIEW_TYPE_HEADER = Int.MIN_VALUE
		const val VIEW_TYPE_FOOTER = Int.MAX_VALUE
	}

	protected abstract fun createItemViewHolder(binding: B, viewType: Int): H

	protected abstract fun bindItemViewHolder(holder: H, position: Int, payloads: MutableList<Any>)

	protected open fun createHeaderViewHolder(binding: B): H {
		throw RuntimeException("If you use footer in the list, you ought to override this method and provide a proper ViewHolder")
	}

	protected open fun createFooterViewHolder(binding: B): H {
		throw RuntimeException("If you use footer in the list, you ought to override this method and provide a proper ViewHolder")
	}

	protected open fun bindHeaderViewHolder(holder: H, payloads: MutableList<Any>) {}

	protected open fun bindFooterViewHolder(holder: H, payloads: MutableList<Any>) {}

	final override fun getItemCount(): Int {
		var itemCount = super.getItemCount()
		if (withHeader) itemCount++
		if (withFooter) itemCount++
		return itemCount
	}

	final override fun getItemViewType(position: Int): Int {
		if (withHeader && isFirstViewPosition(position))
			return VIEW_TYPE_HEADER
		if (withFooter && isLastViewPosition(position))
			return VIEW_TYPE_FOOTER
		return getItemViewTypeInner(getItemPosition(position))
	}

	protected open fun getItemViewTypeInner(position: Int): Int = VIEW_TYPE_ITEM

	final override fun getViewHolder(binding: B, viewType: Int): H {
		if (withHeader && viewType == VIEW_TYPE_HEADER)
			return createHeaderViewHolder(binding)
		if (withFooter && viewType == VIEW_TYPE_FOOTER)
			return createFooterViewHolder(binding)
		return createItemViewHolder(binding, viewType)
	}

	final override fun onBindViewHolder(holder: H, position: Int) {
		onBindViewHolder(holder, position, mutableListOf())
	}

	final override fun onBindViewHolder(holder: H, position: Int, payloads: MutableList<Any>) {
		if (withHeader && isFirstViewPosition(position))
			bindHeaderViewHolder(holder, payloads)
		else if (withFooter && isLastViewPosition(position))
			bindFooterViewHolder(holder, payloads)
		else
			bindItemViewHolder(holder, getItemPosition(position), payloads)
	}

	@UiThread
	fun notifyHeaderChanged() {
		if (withHeader) notifyItemChanged(getFirstViewPosition())
	}

	@UiThread
	fun notifyFooterChanged() {
		if (withFooter) notifyItemChanged(getLastViewPosition())
	}

	@UiThread
	fun notifyItemsChanged() = notifyItemRangeChanged(getFirstItemPosition(), getLastItemPosition())

	@UiThread
	fun notifyItemsChanged(payload: Bundle) = notifyItemRangeChanged(getFirstItemPosition(), getLastItemPosition(), payload)

	@UiThread
	fun notifyListItemChanged(position: Int) = notifyItemChanged(getViewPosition(position))

	@UiThread
	fun notifyListItemChanged(position: Int, payload: Bundle) = notifyItemChanged(getViewPosition(position), payload)

	@UiThread
	fun notifyListItemInserted(position: Int) = notifyItemInserted(getViewPosition(position))

	@UiThread
	fun notifyListItemRemoved(position: Int) = notifyItemRemoved(getViewPosition(position))

	@UiThread
	fun notifyListItemMoved(fromPos: Int, toPos: Int) = notifyItemMoved(getViewPosition(fromPos), getViewPosition(toPos))

	@UiThread
	fun notifyListItemsChanged(startPos: Int, count: Int) = notifyItemRangeChanged(getViewPosition(startPos), count)

	@UiThread
	fun notifyListItemRangeInserted(startPos: Int, count: Int) = notifyItemRangeInserted(getViewPosition(startPos), count)

	@UiThread
	fun notifyListItemRangeRemoved(startPos: Int, count: Int) = notifyItemRangeRemoved(getViewPosition(startPos), count)

	@UiThread
	fun notifyListItemRangeChanged(startPos: Int, count: Int, payload: Any?) = notifyItemRangeChanged(getViewPosition(startPos), count, payload)

	private fun isFirstViewPosition(viewPos: Int) = viewPos == getFirstViewPosition()

	private fun isLastViewPosition(viewPos: Int) = viewPos == getLastViewPosition()

	private fun getFirstViewPosition() = 0

	private fun getLastViewPosition() = itemCount

	private fun getFirstItemPosition() = if (withHeader) 1 else getFirstViewPosition()

	private fun getLastItemPosition() = itemCount - 1

	private fun getItemPosition(viewPosition: Int) = if (withHeader) viewPosition - 1 else viewPosition

	private fun getViewPosition(itemPosition: Int) = if (withHeader) itemPosition + 1 else itemPosition
}