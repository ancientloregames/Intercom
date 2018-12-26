package com.ancientlore.intercom.ui.contact.list

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.BasicFragment

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

	override fun initViewModel(viewModel: ContactListViewModel) {}

	override fun observeViewModel(viewModel: ContactListViewModel) {}
}
