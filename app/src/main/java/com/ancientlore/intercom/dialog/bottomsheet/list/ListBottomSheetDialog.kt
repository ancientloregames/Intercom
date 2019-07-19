package com.ancientlore.intercom.dialog.bottomsheet.list

import android.content.Context
import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.dialog.bottomsheet.MvvmBottomSheetDialog
import com.ancientlore.intercom.widget.list.simple.SimpleListItem
import io.reactivex.internal.disposables.ListCompositeDisposable
import com.ancientlore.intercom.databinding.ListBottomSheetBinding

open class ListBottomSheetDialog: MvvmBottomSheetDialog<ListBottomSheetViewModel>() {

	companion object {
		const val ARG_ITEMS = "arg_items"

		fun newInstance(items: ArrayList<SimpleListItem>): ListBottomSheetDialog {
			val dialog = ListBottomSheetDialog()
			val args = Bundle()
			args.putParcelableArrayList(ARG_ITEMS, items)
			dialog.arguments = args
			return dialog
		}
	}

	interface Listener {
		fun onItemSelected(item: SimpleListItem)
	}
	private var listener: Listener? = null

	private val subscriptions = ListCompositeDisposable()

	override fun getFragmentTag() = "ListBottomSheetDialog"

	override fun getLayoutResId() = R.layout.list_bottom_sheet

	override fun onDestroyView() {
		subscriptions.dispose()

		super.onDestroyView()
	}

	override fun createViewModel(context: Context): ListBottomSheetViewModel {
		val viewModel = ListBottomSheetViewModel(context)

		viewModel.setAdapterItems(getItemsArg())

		subscriptions.add(viewModel.observeItemClicked().subscribe {
			hide()
			listener?.onItemSelected(it)
		})

		return viewModel
	}

	override fun bind(view: View, viewModel: ListBottomSheetViewModel) {
		val binding = ListBottomSheetBinding.bind(view)
		binding.viewModel = viewModel
	}

	fun setListener(listener: Listener) { this.listener = listener }

	private fun getItemsArg() = arguments?.getParcelableArrayList<SimpleListItem>(ARG_ITEMS)
		?: throw RuntimeException("Error! Items list must be passed as an argument!")
}