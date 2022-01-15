package com.ancientlore.intercom.ui.contact.detail

import android.net.Uri
import androidx.annotation.IntDef
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.di.contact.detail.ContactDetailScreenScope
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ContactDetailScreenScope
class ContactDetailViewModel @Inject constructor(
	private val params: ContactDetailParams
	) : BasicViewModel() {

	companion object {
		const val OPTION_AUDIO_CALL = 0
		const val OPTION_VIDEO_CALL = 1
	}

	@IntDef(OPTION_AUDIO_CALL, OPTION_VIDEO_CALL)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Option

	val contactIconField = ObservableField(params.iconUrl)
	val contactNameField = ObservableField(params.name)
	val contactPhoneField = ObservableField(Utils.toHumanReadablePhone(params.id, null))
	val contactStatusField = ObservableField("")
	val contactEmailField = ObservableField("")

	val userInfoLoaded = ObservableBoolean(false)

	private val putToClipboardSubj = PublishSubject.create<String>()
	private val openImageSubj = PublishSubject.create<String>()
	private val openChatFlowSubj = PublishSubject.create<ChatFlowParams>()
	private val closeSubj = PublishSubject.create<Any>()

	private val makeAudioCallSubj = PublishSubject.create<CallViewModel.Params>()
	private val makeVideoCallSubj = PublishSubject.create<CallViewModel.Params>()

	init {
		UserRepository.getItem(params.id, object : CrashlyticsRequestCallback<User>() {

			override fun onSuccess(result: User) {

				if (result.status.isNotEmpty())
					contactStatusField.set(result.status)
				if (result.email.isNotEmpty())
					contactEmailField.set(result.email)

				userInfoLoaded.set(true)
			}
		})
	}

	override fun clean() {
		putToClipboardSubj.onComplete()
		openChatFlowSubj.onComplete()
		makeAudioCallSubj.onComplete()
		makeVideoCallSubj.onComplete()
		closeSubj.onComplete()

		super.clean()
	}

	fun onContactIconClicked() {
		if (params.iconUrl.isNotEmpty())
			openImageSubj.onNext(params.iconUrl)
	}

	fun onContactPhoneClicked() {
		val phone = contactPhoneField.get()!!
		if (phone.isNotEmpty())
			putToClipboardSubj.onNext(phone)
	}

	fun onContactEmailClicked() {
		val status = contactEmailField.get()!!
		if (status.isNotEmpty())
			putToClipboardSubj.onNext(status)
	}

	fun onContactStatusClicked() {
		val status = contactStatusField.get()!!
		if (status.isNotEmpty())
			putToClipboardSubj.onNext(status)
	}

	fun onChatButtonClicked() {
		if (params.openedFromChat) {
			closeSubj.onNext(EmptyObject)
		}
		else {
			val userId = App.backend.getAuthManager().getCurrentUser().id
			openChatFlowSubj.onNext(ChatFlowParams(
				userId = userId,
				title = params.name,
				iconUri = Uri.parse(params.iconUrl),
				chatId = params.chatId ?: "",
				chatType = Chat.TYPE_PRIVATE,
				participants = listOf(userId, params.id)))
		}
	}

	fun onOptionSelected(@Option selectedId: Int) {
		when (selectedId) {
			OPTION_AUDIO_CALL -> makeAudioCallSubj.onNext(
				CallViewModel.Params(
					params.id,
					params.name,
					params.iconUrl
				))
			OPTION_VIDEO_CALL -> makeVideoCallSubj.onNext(
				CallViewModel.Params(
					params.id,
					params.name,
					params.iconUrl
				))
		}
	}

	fun observePutToClipboardRequest() = putToClipboardSubj as Observable<String>

	fun observeOpenChatFlowRequest() = openChatFlowSubj as Observable<ChatFlowParams>

	fun openImageRequest() = openImageSubj as Observable<String>

	fun observeMakeAudioCallRequest() = makeAudioCallSubj as Observable<CallViewModel.Params>

	fun observeMakeVideoCallRequest() = makeVideoCallSubj as Observable<CallViewModel.Params>

	fun observeCloseRequest() = closeSubj as Observable<Any>
}