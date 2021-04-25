package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel(private val listAdapter: ContactListAdapter) : BasicViewModel() {

	private val openContactDetailSub = PublishSubject.create<Contact>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		openContactDetailSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) {
				openContactDetailSub.onNext(contact)
			}
		})

		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>>{
			override fun onSuccess(result: List<Contact>) {
				listAdapter.setItems(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun observeOpenContactDetail() = openContactDetailSub as Observable<Contact>

	fun filter(text: String) = listAdapter.filter(text)
}