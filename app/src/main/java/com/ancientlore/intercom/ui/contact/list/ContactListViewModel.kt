package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ContactListAdapter

	private val contactSelectedEvent = PublishSubject.create<Contact>()

	fun setListAdapter(listAdapter: ContactListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) = contactSelectedEvent.onNext(contact)
		})
		loadContactList()
	}

	fun observeContactSelected() = contactSelectedEvent as Observable<Contact>

	private fun loadContactList() {
		ContactRepository.getAll(object : SimpleRequestCallback<List<Contact>>() {
			override fun onSuccess(result: List<Contact>) {
				listAdapter.setItems(result)
			}
		})
	}
}