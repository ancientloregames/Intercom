package com.ancientlore.intercom.ui.contact.list

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import kotlinx.android.synthetic.main.contact_list_ui.*

class ContactListFragment : BasicFragment<ContactListViewModel, ContactListUiBinding>() {

	companion object {
		fun newInstance() = ContactListFragment()
	}

	override fun getLayoutResId() = R.layout.contact_list_ui

	override fun createViewModel() = ContactListViewModel()

	override fun bind(view: View, viewModel: ContactListViewModel) {
		dataBinding = ContactListUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: ContactListViewModel) {
		val listAdapter = ContactListAdapter(context!!, mutableListOf())
		listView.adapter = listAdapter
		viewModel.setListAdapter(listAdapter)
	}

	override fun observeViewModel(viewModel: ContactListViewModel) {
		subscriptions.add(viewModel.observeToastRequest()
			.subscribe { showToast(it) })
		subscriptions.add(viewModel.observeChatOpenById()
			.subscribe { navigator?.openChatDetail(it) })
	}
}
