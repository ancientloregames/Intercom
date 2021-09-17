package com.ancientlore.intercom.ui.contact.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager

class ContactListFragment : FilterableFragment<ContactListViewModel, ContactListUiBinding>() {

	companion object {
		fun newInstance() = ContactListFragment()
	}

	override fun getOpenAnimation(): Int = R.anim.slide_in_bottom

	override fun getCloseAnimation(): Int = R.anim.slide_out_bottom

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.contact_list_menu

	override fun getLayoutResId() = R.layout.contact_list_ui

	override fun createDataBinding(view: View) = ContactListUiBinding.bind(view)

	override fun createViewModel() = ContactListViewModel(requireContext())

	override fun init(viewModel: ContactListViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		viewModel.init()

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.listAdapter

		subscriptions.add(viewModel.observeOpenContactDetail()
			.subscribe {
				navigator?.openContactDetail(it)
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}
}
