package com.ancientlore.intercom.utils.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ancientlore.intercom.ui.chat.flow.ChatFlowAdapter

fun RecyclerView.enableChatBehavior() {
	if (adapter == null)
		throw RuntimeException("Adapter must be attached bofore changing behavior")

	val a = adapter!!
	val lm = if (layoutManager is LinearLayoutManager)
		layoutManager as LinearLayoutManager
	else LinearLayoutManager(context)
	// FIXME list header counts as a first visible item
	val constraint = if (a is ChatFlowAdapter) 0 else RecyclerView.NO_POSITION

	lm.stackFromEnd = true
	a.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			super.onItemRangeInserted(positionStart, itemCount)
			val lastVisiblePosition = lm.findLastCompletelyVisibleItemPosition()
			if (lastVisiblePosition == constraint
				|| positionStart >= a.itemCount - 1
				&& lastVisiblePosition == positionStart - 1)
				scrollToPosition(positionStart + itemCount - 1)
		}
	})
}