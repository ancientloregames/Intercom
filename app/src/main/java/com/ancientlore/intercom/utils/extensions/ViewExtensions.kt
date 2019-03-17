package com.ancientlore.intercom.utils.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.enableChatBehavior() {
	if (adapter == null)
		throw RuntimeException("Adapter must be attached bofore changing behavior")

	val a = adapter!!
	val lm = if (layoutManager is LinearLayoutManager)
		layoutManager as LinearLayoutManager
	else LinearLayoutManager(context)

	lm.stackFromEnd = true
	a.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			super.onItemRangeInserted(positionStart, itemCount)
			val lastVisiblePosition = lm.findLastCompletelyVisibleItemPosition()
			if (lastVisiblePosition == -1
				|| positionStart >= a.itemCount - 1
				&& lastVisiblePosition == positionStart - 1)
				scrollToPosition(positionStart)
		}
	})
}