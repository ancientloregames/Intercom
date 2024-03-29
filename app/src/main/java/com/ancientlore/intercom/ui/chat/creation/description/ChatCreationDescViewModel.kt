package com.ancientlore.intercom.ui.chat.creation.description

import android.content.Context
import android.net.Uri
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatCreationDescViewModel(context: Context)
	: FilterableViewModel<ChatCreationDescAdapter>(ChatCreationDescAdapter(context)) {

	companion object {
		const val TOAST_REQUIRED_NAME_ERR = 0
	}

	val groupIconField = ObservableField(Uri.EMPTY)
	val groupNameField = ObservableField("")

	private val createChatSub = PublishSubject.create<ChatFlowParams>()
	private val openGallarySub = PublishSubject.create<Any>()

	private val contacts = mutableListOf<Contact>()

	private var iconUri: Uri? = null

	override fun clean() {
		createChatSub.onComplete()
		openGallarySub.onComplete()

		super.clean()
	}

	fun init(contacts: List<Contact>) {
		this.contacts.addAll(contacts)
		listAdapter.setItems(contacts)
	}

	fun onNextClicked() = tryCreateGroupChat()

	fun onIconClicked() = openGallarySub.onNext(EmptyObject)

	fun observeCreateChatRequest() = createChatSub as Observable<ChatFlowParams>

	fun observeOpenGallaryRequest() = openGallarySub as Observable<Any>

	fun onChatIconSelected(uri: Uri) {
		this.iconUri = uri
		groupIconField.set(uri)
	}

	private fun tryCreateGroupChat() {
		val groupName = groupNameField.get()!!
		if (groupName.isNotEmpty()) {

			val userId = App.backend.getAuthManager().getCurrentUser().id

			createChatSub.onNext(ChatFlowParams(
				userId = userId,
				title = groupName,
				chatType = Chat.TYPE_GROUP,
				iconUri = iconUri ?: Uri.EMPTY,
				participants = listOf(userId).plus(contacts.map { it.getIdentity() })
			))
		}
		else toastRequest.onNext(TOAST_REQUIRED_NAME_ERR)
	}
}