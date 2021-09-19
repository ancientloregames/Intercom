package com.ancientlore.intercom.ui.boadcast.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.BroadcastListUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager

class BroadcastListFragment: FilterableFragment<BroadcastListViewModel, BroadcastListUiBinding>() {

	companion object {

		fun newInstance() = BroadcastListFragment()
	}

	override fun getToolbar(): Toolbar  = dataBinding.toolbar

	override fun getToolbarMenuResId(): Int  = R.menu.broadcast_list_menu

	override fun getLayoutResId(): Int = R.layout.broadcast_list_ui

	override fun createDataBinding(view: View) = BroadcastListUiBinding.bind(view)

	override fun createViewModel() = BroadcastListViewModel(requireContext())

	override fun init(viewModel: BroadcastListViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.listAdapter

		subscriptions.add(viewModel.openBroadcastRequest()
			.subscribe {
				navigator?.openChatFlow(it)
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}
}