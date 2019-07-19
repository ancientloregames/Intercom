package com.ancientlore.intercom.dialog.bottomsheet.list

import android.content.Context
import com.ancientlore.intercom.widget.list.BasicListViewModel
import com.ancientlore.intercom.widget.list.simple.SimpleListAdapter
import com.ancientlore.intercom.widget.list.simple.SimpleListItem

class ListBottomSheetViewModel(context: Context): BasicListViewModel<SimpleListItem>(context) {
	override fun createListAdapter(context: Context) =
		SimpleListAdapter(context)
}