package com.ancientlore.intercom.ui.contact.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.contact_list_ui.*

class ContactListFragment : BasicFragment<ContactListViewModel, ContactListUiBinding>() {

	companion object {
		fun newInstance() = ContactListFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.contact_list_ui

	override fun createViewModel() = ContactListViewModel()

	override fun bind(view: View, viewModel: ContactListViewModel) {
		dataBinding = ContactListUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton(View.OnClickListener {
				close()
			})
		}

		swipableLayout.setListener { close() }

		listView.adapter = ContactListAdapter(context!!, mutableListOf())
	}

	override fun initViewModel(viewModel: ContactListViewModel) {
		viewModel.setListAdapter(listView.adapter as ContactListAdapter)
	}

	override fun observeViewModel(viewModel: ContactListViewModel) {
		subscriptions.add(viewModel.observeToastRequest()
			.subscribe { showToast(it) })
		subscriptions.add(viewModel.observeChatOpen()
			.subscribe {
				close()
				navigator?.openChatFlow(it)
			})
	}
}
