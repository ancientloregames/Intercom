package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.contact.detail.ContactDetailParams
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel(context: Context)
	: FilterableViewModel<ContactListAdapter>(ContactListAdapter(context)) {

	val contactListIsEmpty = ObservableBoolean(false)

	private val openContactDetailSub = PublishSubject.create<ContactDetailParams>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		openContactDetailSub.onComplete()
		repositorySub?.remove()
		listAdapter.setListener(null)

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) {
				openContactDetailSub.onNext(ContactDetailParams(
					contact.phone,
					contact.name,
					contact.iconUrl,
					false,
					contact.chatId
				))
			}
		})

		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>>{
			override fun onSuccess(result: List<Contact>) {
				runOnUiThread {
					contactListIsEmpty.set(result.isEmpty())
					val validItems = result.filter { it.phone.isNotEmpty() } // TODO move to adapted by generic Identifiable
					listAdapter.setItems(validItems)
				}
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun observeOpenContactDetail() = openContactDetailSub as Observable<ContactDetailParams>
}