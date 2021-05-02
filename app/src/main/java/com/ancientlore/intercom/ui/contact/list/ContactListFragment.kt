package com.ancientlore.intercom.ui.contact.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.contact_list_ui.listView
import kotlinx.android.synthetic.main.contact_list_ui.swipableLayout
import kotlinx.android.synthetic.main.contact_list_ui.toolbar

class ContactListFragment : FilterableFragment<ContactListViewModel, ContactListUiBinding>() {

	companion object {
		fun newInstance() = ContactListFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getToolbar(): Toolbar = toolbar

	override fun getToolbarMenuResId() = R.menu.contact_list_menu

	override fun getLayoutResId() = R.layout.contact_list_ui

	override fun createViewModel() = ContactListViewModel(listView.adapter as ContactListAdapter)

	override fun bind(view: View, viewModel: ContactListViewModel) {
		dataBinding = ContactListUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close() }

		listView.adapter = ContactListAdapter(requireContext())
	}

	override fun initViewModel(viewModel: ContactListViewModel) {
		viewModel.init()
	}

	override fun observeViewModel(viewModel: ContactListViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeOpenContactDetail()
			.subscribe {
				// TODO open contact detail not closing this ui
			})
	}
}
