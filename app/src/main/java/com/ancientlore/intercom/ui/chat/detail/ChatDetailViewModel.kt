package com.ancientlore.intercom.ui.chat.detail

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescAdapter
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ChatDetailViewModel @Inject constructor(
	context: Context,
	private val params: Params
	) : FilterableViewModel<ChatCreationDescAdapter>() {

	companion object {
		const val TOAST_SET_PHOTO_ERR = 0
	}

	val chatIconField = ObservableField(params.iconUri)
	val chatNameField = ObservableField(params.title)

	val showProcess = ObservableBoolean(false)
	val allowModification = ObservableBoolean(false) // TODO finish implementing feature (update chat flow state)
	val modified = ObservableBoolean(false)

	private val openImageViewerSubj = PublishSubject.create<Uri>()
	private val openGallarySubj = PublishSubject.create<Any>()
	private val closeSubj = PublishSubject.create<Any>()

	private val listAdapter: ChatCreationDescAdapter = ChatCreationDescAdapter(context)

	init {
		ContactRepository.getItems(params.participants, object : CrashlyticsRequestCallback<List<Contact>>() {

			override fun onSuccess(result: List<Contact>) {
				runOnUiThread {
					listAdapter.setItems(result)
				}
			}
		})

		chatNameField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

			override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
				//modified.set(true)
			}
		})
	}

	override fun getListAdapter(): ChatCreationDescAdapter = listAdapter

	override fun clean() {
		openGallarySubj.onComplete()
		closeSubj.onComplete()

		super.clean()
	}

	fun onIconClicked() {
		if (allowModification.get()) {
			openGallarySubj.onNext(EmptyObject)
		}
		else {
			val chatIconUri = chatIconField.get()
			if (chatIconUri != null && chatIconUri != Uri.EMPTY)
				openImageViewerSubj.onNext(chatIconUri!!)
		}
	}

	fun onDoneClicked() {
		showProcess.set(true)

		val currentIcon = chatIconField.get()!!
		val currentName = chatNameField.get()!!

		val chatUpdate = Chat(
			iconUrl = if (currentIcon != params.iconUri) currentIcon.toString() else "",
			name = if (currentName != params.title) currentName else ""
		)

		ChatRepository.updateItem(chatUpdate, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				showProcess.set(false)
				closeSubj.onNext(EmptyObject)
			}
			override fun onFailure(error: Throwable) {
				showProcess.set(false)
				toastRequest.onNext(TOAST_SET_PHOTO_ERR)
			}
		})
	}

	fun onChatIconSelected(uri: Uri) {
		chatIconField.set(uri)
		modified.set(true)
	}

	fun openImageViewerRequest() = openImageViewerSubj as io.reactivex.Observable<Uri>

	fun observeOpenGallaryRequest() = openGallarySubj as io.reactivex.Observable<Any>

	fun observeCloseRequest() = closeSubj as io.reactivex.Observable<Any>

	data class Params(val title: String = "",
	                  val iconUri: Uri = Uri.EMPTY,
	                  val participants: List<String> = emptyList()) : Parcelable {

		constructor(parcel: Parcel) : this(
			parcel.readString(),
			parcel.readParcelable(Uri::class.java.classLoader),
			parcel.createStringArrayList())

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			parcel.writeString(title)
			parcel.writeParcelable(iconUri, flags)
			parcel.writeStringList(participants)
		}

		override fun describeContents(): Int {
			return 0
		}

		companion object CREATOR : Parcelable.Creator<Params> {
			override fun createFromParcel(parcel: Parcel): Params {
				return Params(parcel)
			}

			override fun newArray(size: Int): Array<Params?> {
				return arrayOfNulls(size)
			}
		}
	}
}