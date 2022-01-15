package com.ancientlore.intercom.ui.chat.creation.description

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ChatCreationDescViewModel @Inject constructor(
	context: Context,
	params: Params
) : FilterableViewModel<ChatCreationDescAdapter>() {

	companion object {
		const val TOAST_REQUIRED_NAME_ERR = 0
	}

	val groupIconField = ObservableField(Uri.EMPTY)
	val groupNameField = ObservableField("")

	private val createChatSub = PublishSubject.create<ChatFlowParams>()
	private val openGallarySub = PublishSubject.create<Any>()

	private val listAdapter: ChatCreationDescAdapter = ChatCreationDescAdapter(context)

	private val contacts = mutableListOf<Contact>()

	private var iconUri: Uri? = null

	override fun getListAdapter(): ChatCreationDescAdapter = listAdapter

	override fun clean() {
		createChatSub.onComplete()
		openGallarySub.onComplete()

		super.clean()
	}

	init {
		this.contacts.addAll(params.contacts)
		listAdapter.setItems(params.contacts)
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

	data class Params(val contacts: List<Contact>): Parcelable {

		constructor(parcel: Parcel) : this(parcel.createTypedArrayList(Contact))

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			parcel.writeTypedList(contacts)
		}

		override fun describeContents(): Int = 0

		companion object CREATOR : Parcelable.Creator<Params> {
			override fun createFromParcel(parcel: Parcel): Params = Params(parcel)

			override fun newArray(size: Int): Array<Params?> = arrayOfNulls(size)
		}
	}
}