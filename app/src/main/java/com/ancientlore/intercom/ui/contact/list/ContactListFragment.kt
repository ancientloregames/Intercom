package com.ancientlore.intercom.ui.contact.list

import android.provider.ContactsContract
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.Contact
import com.ancientlore.intercom.databinding.ContactListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.getContactPhones
import com.ancientlore.intercom.utils.safeClose
import kotlinx.android.synthetic.main.contact_list_ui.*
import java.util.ArrayList

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

	override fun initView() {
		listView.adapter = ContactListAdapter(listView.context, getContactList())
	}

	override fun observeViewModel(viewModel: ContactListViewModel) {}

	private fun getContactList(): List<Contact> {
		val list = ArrayList<Contact>()

		if (context == null)
			return list

		val resolver = context!!.contentResolver
		val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					val item = Contact()
					item.id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
					item.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
					item.imageUrl = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) ?: ""
					val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
					if (hasPhone) {
						val phones = resolver.getContactPhones(item.id)
						item.phone = phones[0]
					}
					list.add(item)
				}
			} finally {
				cursor.safeClose()
			}
		}

		return list
	}
}
