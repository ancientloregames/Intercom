package com.ancientlore.intercom.ui.chat.creation

import android.content.Context
import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.chat.list.ChatListViewModel
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ChatCreationViewModel @Inject constructor(
	context: Context
) : FilterableViewModel<ChatCreationAdapter>() {

	companion object {
		const val OPTION_SHOW_USERS = 0
	}

	val contactListIsEmpty = ObservableBoolean(false)

	private val openChatSub = PublishSubject.create<ChatFlowParams>()
	private val createGroupSub = PublishSubject.create<Any>()
	private val createBroadcastSub = PublishSubject.create<Any>()
	private val addContactSub = PublishSubject.create<Any>()
	private val updateContactCountSub = PublishSubject.create<Int>()

	private val listAdapter: ChatCreationAdapter = ChatCreationAdapter(context)

	private var repositorySub: RepositorySubscription? = null

	override fun getListAdapter(): ChatCreationAdapter = listAdapter

	override fun clean() {
		listAdapter.setListener(null)
		openChatSub.onComplete()
		createGroupSub.onComplete()
		createBroadcastSub.onComplete()
		addContactSub.onComplete()
		updateContactCountSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ChatCreationAdapter.Listener {
			override fun onContactSelected(contact: Contact) {
				val userId = App.backend.getAuthManager().getCurrentUserId()
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
			override fun onCreateBroadcast() {
				createBroadcastSub.onNext(EmptyObject)
			}
		})
		attachDataListener()
	}

	private fun attachDataListener() {
		repositorySub = ContactRepository.attachListener(object : RequestCallback<List<Contact>> {
			override fun onSuccess(result: List<Contact>) {
				runOnUiThread {
					contactListIsEmpty.set(result.isEmpty())

					val validItems = result.filter { it.phone.isNotEmpty() } // TODO move to adapted by generic Identifiable
					listAdapter.setItems(validItems)
				}
				updateContactCountSub.onNext(result.size)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun onOptionSelected(@ChatListViewModel.Option selectedId: Int) {
		when (selectedId) {
			OPTION_SHOW_USERS -> { // TODO delete. it's just for the demo
				UserRepository.getAll(object : CrashlyticsRequestCallback<List<User>>() {
					override fun onSuccess(result: List<User>) {
						val currentUserId = App.backend.getAuthManager().getCurrentUserId()
						val contacts = result
							.filter { it.id != currentUserId }
							.map { it.toContact() }

						runOnUiThread {
							contactListIsEmpty.set(result.isEmpty())
							listAdapter.setItems(contacts)
						}
					}
				})
			}
		}
	}

	fun observeChatOpen() = openChatSub as Observable<ChatFlowParams>

	fun observeCreateGroup() = createGroupSub as Observable<Any>

	fun createBroadcastRequest() = createBroadcastSub as Observable<Any>

	fun observeAddContact() = addContactSub as Observable<Any>

	fun observeUpdateContactCount() = updateContactCountSub as Observable<Int>

	private fun User.toContact(): Contact {
		return Contact(
			phone, name, "", iconUrl
		)
	}
}