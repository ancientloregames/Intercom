package com.ancientlore.intercom.ui.contact.list

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.contact_list_ui.listView
import kotlinx.android.synthetic.main.contact_list_ui.swipableLayout
import kotlinx.android.synthetic.main.contact_list_ui.toolbar

class ContactListFragment : BasicFragment<ContactListViewModel, ContactListUiBinding>() {

	companion object {
		fun newInstance() = ContactListFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.contact_list_ui

	override fun createViewModel() = ContactListViewModel(listView.adapter as ContactListAdapter)

	override fun bind(view: View, viewModel: ContactListViewModel) {
		dataBinding = ContactListUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		initToolbarMenu()
		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close() }

		listView.adapter = ContactListAdapter(context!!, mutableListOf())
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

	private fun initToolbarMenu() {
		navigator?.createToolbarMenu(toolbar) { menu ->
			activity?.menuInflater?.inflate(R.menu.chat_flow_menu, menu)
			val search = menu.findItem(R.id.search).actionView as SearchView
			search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
				override fun onQueryTextSubmit(query: String?): Boolean {
					query?.let { constraint ->
						viewModel.filter(constraint)
					}
					return true
				}

				override fun onQueryTextChange(newText: String?): Boolean {
					newText
						?.takeIf { it.length > 1 }
						?.let { viewModel.filter(it) }
						?: run { viewModel.filter("") }
					return true
				}
			})
		}
	}
}
