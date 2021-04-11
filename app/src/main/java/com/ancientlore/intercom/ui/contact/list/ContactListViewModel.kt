package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel() {

	private var listAdapter: ContactListAdapter? = null

	private val openChatSub = PublishSubject.create<ChatFlowParams>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		openChatSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init(listAdapter: ContactListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) {
				openChatSub.onNext(ChatFlowParams(
					userId = App.backend.getAuthManager().getCurrentUser().id,
					title = contact.name,
					contactId = contact.id))
			}
		})
		attachDataListener()
	}

	private fun attachDataListener() {
		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>>{
			override fun onSuccess(result: List<Contact>) {
				listAdapter?.setItems(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun observeChatOpen() = openChatSub as Observable<ChatFlowParams>

	fun filter(text: String) {
		listAdapter?.filter(text)
	}
}