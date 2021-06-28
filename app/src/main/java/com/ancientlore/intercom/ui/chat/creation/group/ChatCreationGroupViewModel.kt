package com.ancientlore.intercom.ui.chat.creation.group

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatCreationGroupViewModel(listAdapter: ChatCreationGroupAdapter,
                                 private val selectedListAdapter: ChatCreationSelectedAdapter)
	: FilterableViewModel<ChatCreationGroupAdapter>(listAdapter) {

	private val openNextSub = PublishSubject.create<List<Contact>>()

	private var repositorySub: RepositorySubscription? = null

	private var adapterListener = object : ChatCreationGroupAdapter.Listener {
		override fun onContactSelected(contact: Contact) {
			contact.checked = contact.checked.not()

			if (selectedListAdapter.hasContact(contact))
				selectedListAdapter.deleteItem(contact)
			else
				selectedListAdapter.prependItem(contact)
		}
	}

	private var selectedAdapterListener = object : ChatCreationSelectedAdapter.Listener {
		override fun onContactSelected(contact: Contact) {

			selectedListAdapter.deleteItem(contact)
			listAdapter.switchCheckBoxItem(contact)
		}
	}

	override fun clean() {
		openNextSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init() {
		listAdapter.setListener(adapterListener)

		selectedListAdapter.setListener(selectedAdapterListener)

		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>> {
			override fun onSuccess(result: List<Contact>) {
				listAdapter.setItems(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun onNextClicked() = openNextSub.onNext(selectedListAdapter.getContacts())

	fun observeNextRequest() = openNextSub as Observable<List<Contact>>
}