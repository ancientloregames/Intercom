package com.ancientlore.intercom.ui.chat.creation

import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatCreationViewModel(listAdapter: ChatCreationAdapter)
	: FilterableViewModel<ChatCreationAdapter>(listAdapter) {

	val contactListIsEmpty = ObservableBoolean(false)

	private val openChatSub = PublishSubject.create<ChatFlowParams>()
	private val createGroupSub = PublishSubject.create<Any>()
	private val addContactSub = PublishSubject.create<Any>()
	private val updateContactCountSub = PublishSubject.create<Int>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		openChatSub.onComplete()
		createGroupSub.onComplete()
		addContactSub.onComplete()
		updateContactCountSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ChatCreationAdapter.Listener {
			override fun onContactSelected(contact: Contact) {
				val userId = App.backend.getAuthManager().getCurrentUser().id
				openChatSub.onNext(
					ChatFlowParams(
						userId = userId,
						title = contact.name,
						iconUri = contact.iconUri,
						chatId = contact.chatId,
						chatType = Chat.TYPE_PRIVATE,
						participants = listOf(userId, contact.getIdentity()))
				)
			}
			override fun onCreateGroup() {
				createGroupSub.onNext(EmptyObject)
			}
			override fun onAddContact() {
				addContactSub.onNext(EmptyObject)
			}
		})
		attachDataListener()
	}

	private fun attachDataListener() {
		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>> {
			override fun onSuccess(result: List<Contact>) {
				runOnUiThread {
					contactListIsEmpty.set(result.isEmpty())
					listAdapter.setItems(result)
				}
				updateContactCountSub.onNext(result.size)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun observeChatOpen() = openChatSub as Observable<ChatFlowParams>

	fun observeCreateGroup() = createGroupSub as Observable<Any>

	fun observeAddContact() = addContactSub as Observable<Any>

	fun observeUpdateContactCount() = updateContactCountSub as Observable<Int>
}