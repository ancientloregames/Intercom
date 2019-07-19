package com.ancientlore.intercom.ui.dialog.attach

import android.os.Bundle
import com.ancientlore.intercom.R
import com.ancientlore.intercom.dialog.bottomsheet.list.ListBottomSheetDialog
import com.ancientlore.intercom.widget.list.simple.SimpleListItem

class AttachBottomSheetDialog : ListBottomSheetDialog() {

	companion object {
		fun newInstance(): AttachBottomSheetDialog {
			return AttachBottomSheetDialog().apply {
				arguments = Bundle().apply {
					putParcelableArrayList(ARG_ITEMS, createItemList())
				}
			}
		}
	}

	private fun createItemList() : ArrayList<SimpleListItem> {
		return arrayListOf<SimpleListItem>().apply {
			add(SimpleListItem(R.id.im_attach_file, R.string.document, R.drawable.ic_file))
		}
	}
}