package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel() {

	private val contactSelectedEvent = PublishSubject.create<Contact>()

	fun init(listAdapter: ContactListAdapter) {
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) = contactSelectedEvent.onNext(contact)
		})
	}

	fun observeContactSelected() = contactSelectedEvent as Observable<Contact>
}