package com.ancientlore.intercom.ui.chat.flow

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.C.ICON_DIR_PATH
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.databinding.ChatFlowUiBinding
import com.ancientlore.intercom.dialog.bottomsheet.list.ListBottomSheetDialog
import com.ancientlore.intercom.service.ChatIconUploadService
import com.ancientlore.intercom.service.FileUploadService
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowViewModel.Companion.OPTION_AUDIO_CALL
import com.ancientlore.intercom.ui.chat.flow.ChatFlowViewModel.Companion.OPTION_VIDEO_CALL
import com.ancientlore.intercom.ui.dialog.attach.AttachBottomSheetDialog
import com.ancientlore.intercom.ui.dialog.option.message.MessageOptionMenuDialog
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.*
import com.ancientlore.intercom.view.MessageInputManager
import com.ancientlore.intercom.widget.list.simple.SimpleListItem
import java.io.File

class ChatFlowFragment : FilterableFragment<ChatFlowViewModel, ChatFlowUiBinding>() {

	companion object {
		const val INTENT_GET_FILES = 101
		const val INTENT_GET_IMAGES = 102

		private const val ARG_PARAMS = "params"

		fun newInstance(params: ChatFlowParams) : ChatFlowFragment {
			return ChatFlowFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : ChatFlowParams by lazy { arguments?.getParcelable<ChatFlowParams>(ARG_PARAMS)
		?: throw RuntimeException("Chat id is a mandotory arg") }

	private val sendButtonAnimDuration: Long by lazy {
		resources.getInteger(R.integer.defaultShowHideAnimationDuration).toLong()
	}

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId(): Int {
		return if (params.chatType == Chat.TYPE_PRIVATE) // TODO really need to separate entities
			R.menu.chat_flow_menu
		else R.menu.chat_flow_group_menu
	}

	override fun getLayoutResId() = R.layout.chat_flow_ui

	override fun createDataBinding(view: View) = ChatFlowUiBinding.bind(view)

	override fun createViewModel() = ChatFlowViewModel(requireContext(), params)

	override fun init(viewModel: ChatFlowViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		setHasOptionsMenu(true)

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		with(dataBinding.listView) {
			adapter = viewModel.listAdapter

			enableChatBehavior()

			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				private var prevLastVisibleItemPos: Int  = 0

				override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
					super.onScrollStateChanged(recyclerView, newState)

					val lastVisibleItemPos = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

					if (lastVisibleItemPos != prevLastVisibleItemPos) {
						prevLastVisibleItemPos = lastVisibleItemPos
						viewModel.onLastVisibleItemChanged(lastVisibleItemPos)
					}

					if (!recyclerView.canScrollVertically(-1)
						&& newState == RecyclerView.SCROLL_STATE_IDLE
						&& adapter!!.itemCount > 1) {
						viewModel.onScrolledToTop()
					}
				}
			})
		}
		// FIXME should set via viewModel
		viewModel.onContactStatusChanged(getString(R.string.member_count, params.participants.size))

		subscriptions.add(viewModel.listAdapter.fileOpenRequest()
			.subscribe {
				navigator?.openFileViewer(it)
			})
		subscriptions.add(viewModel.listAdapter.imageOpenRequest()
			.subscribe {
				navigator?.openImageViewer(it)
			})
		subscriptions.add(viewModel.listAdapter.optionMenuOpenRequest()
			.subscribe {
				openMessageMenu(it)
			})
		subscriptions.add(viewModel.makeAudioCallRequest()
			.subscribe {
				navigator?.openAudioCallOffer(it)
			})
		subscriptions.add(viewModel.makeVideoCallRequest()
			.subscribe {
				navigator?.openVideoCallOffer(it)
			})
		subscriptions.add(viewModel.openAttachmentMenuRequest()
			.subscribe {
				openAttachMenu()
			})
		subscriptions.add(viewModel.recordAudioRequest()
			.subscribe {
				recordAudio()
			})
		subscriptions.add(viewModel.uploadIconRequest()
			.subscribe {
				uploadIcon(it)
			})
		subscriptions.add(viewModel.openChatDetailRequest()
			.subscribe {
				navigator?.openChatDetail(params)
			})
		subscriptions.add(viewModel.openContactDetailRequest()
			.subscribe {
				navigator?.openContactDetail(it)
			})
		subscriptions.add(viewModel.setContactStatusOnlineRequest()
			.subscribe {
				viewModel.onContactStatusChanged(getString(R.string.online))
			})
		subscriptions.add(viewModel.setContactStatusLastSeenRequest()
			.subscribe { lastSeen ->
				viewModel.onContactStatusChanged(getString(R.string.last_seen, lastSeen))
			})
		subscriptions.add(viewModel.scrollToPositionRequest()
			.subscribe {
				dataBinding.listView.smoothScrollToPosition(it)
			})
		subscriptions.add(viewModel.pickFileRequest()
			.subscribe {
				navigator?.openFilePicker(this, INTENT_GET_FILES)
			})
		subscriptions.add(viewModel.pickImageRequest()
			.subscribe {
				navigator?.openImagePicker(this, INTENT_GET_IMAGES)
			})
		subscriptions.add(viewModel.showSendButtonRequest()
			.subscribe {
				showSendHideRecordButton(it)
			})

		if (permissionManager!!.allowedAudioMessage())
			viewModel.attachInputPanelManager(MessageInputManager(dataBinding.contentLayout))
	}

	private fun showSendHideRecordButton(show: Boolean) {
		if (show) {
			if (dataBinding.sendButton.visibility == View.GONE) {
				dataBinding.sendButton.animate()
					.scaleX(1f).scaleY(1f)
					.setDuration(sendButtonAnimDuration).setInterpolator(LinearInterpolator())
					.withStartAction { dataBinding.sendButton.visibility = View.VISIBLE }
					.start()
			}
		} else if (dataBinding.sendButton.visibility == View.VISIBLE) {
			dataBinding.sendButton.animate()
				.scaleX(0f).scaleY(0f)
				.setDuration(sendButtonAnimDuration).setInterpolator(LinearInterpolator())
				.withEndAction { dataBinding.sendButton.visibility = View.GONE }
				.start()
		}
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.audioCall -> {
				viewModel.onOptionSelected(OPTION_AUDIO_CALL)
				true
			}
			R.id.videoCall -> {
				viewModel.onOptionSelected(OPTION_VIDEO_CALL)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			ChatFlowViewModel.TOAST_CHAT_CREATION_ERR -> R.string.alert_error_creating_chat
			ChatFlowViewModel.TOAST_MSG_SEND_ERR -> R.string.alert_error_send_message
			ChatFlowViewModel.TOAST_MSG_DELETED -> R.string.message_deleted
			ChatFlowViewModel.TOAST_MSG_DELETED_NOT -> R.string.message_deleted_not
			ChatFlowViewModel.TOAST_MSG_UNDELETABLE -> R.string.alert_message_undeletable
			ChatFlowViewModel.TOAST_FILE_TOO_BIG -> R.string.alert_error_attach_file_big
			else -> super.getToastStringRes(toastId)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		val handled = if (resultCode == RESULT_OK && data != null)
			handleActivityResult(requestCode, data)
		else {
			showToast(R.string.alert_error_attach_file)
			false
		}

		if (!handled)
			super.onActivityResult(requestCode, resultCode, data)
	}

	private fun handleActivityResult(requestCode: Int, data: Intent) : Boolean {
		return when (requestCode) {
			INTENT_GET_IMAGES -> {
				val uri = getIntentResult(data)
				val fileData = uri.getFileData(context!!.contentResolver)
				val file = File(context!!.getAppCacheDir(), fileData.name)

				if (!file.exists() && file.createNewFile())
					ImageUtils.compressImage(context!!.contentResolver, uri, C.MAX_ATTACH_IMG_SIZE_PX, file)

				if (file.exists()) {
					viewModel.handleAttachedImage(fileData, Uri.fromFile(file))
				} else Utils.logError(RuntimeException("Failed to create"))
				true
			}
			INTENT_GET_FILES -> {
				val uri = getIntentResult(data)
				val fileData = uri.getFileData(context!!.contentResolver)
				viewModel.handleAttachedFile(fileData)
				true
			}
			else -> false
		}
	}

	private fun getIntentResult(intent: Intent) : Uri {
		// TODO multiple selection case (clipData)
		return intent.data ?: Uri.EMPTY
	}

	private fun openMessageMenu(message: Message) {
		activity?.run {

			val userId = App.backend.getAuthManager().getCurrentUserId()
			//TODO temporary check. Change to ChatListFragment-like when more options will be added
			if (message.undeletable.not() && message.senderId == userId) {
				val dialog = MessageOptionMenuDialog.newInstance()

				dialog.listener = object : MessageOptionMenuDialog.Listener {
					override fun onDeleteClicked() {
						viewModel.onMessageMenuOptionSelected(message, ChatFlowViewModel.ITEM_OPTION_DELETE)
					}
				}

				dialog.show(supportFragmentManager)
			}
		}
	}

	private fun openAttachMenu() {

		activity?.run {
			AttachBottomSheetDialog.newInstance().apply {
				setListener(object : ListBottomSheetDialog.Listener {
					override fun onItemSelected(item: SimpleListItem) {
						onAttachMenuItemSelected(item.id)
					}
				})
			}.show(supportFragmentManager)
		}
	}

	private fun onAttachMenuItemSelected(id: Int) {
		when (id) {
			R.id.im_attach_picture -> viewModel.onAttachMenuOptionSelected(ChatFlowViewModel.ATTACH_OPTION_IMAGE)
			R.id.im_attach_file -> viewModel.onAttachMenuOptionSelected(ChatFlowViewModel.ATTACH_OPTION_FILE)
			else -> {
				showToast(R.string.error)
				Utils.logError(RuntimeException("Error! Unknown attach source: id $id"))
			}
		}
	}

	private fun recordAudio() {
		permissionManager?.requestPermissionAudioMessage(Runnable1 { granted ->
			if (granted) {
				viewModel.attachInputPanelManager(MessageInputManager(dataBinding.contentLayout))
			}
		})
	}

	private fun uploadIcon(chatId: String) {
		permissionManager?.requestPermissionWriteStorage { granted ->
			if (granted) {
				activity?.run {
					startService(Intent(this, ChatIconUploadService::class.java)
						.putParcelableArrayListExtra(FileUploadService.EXTRA_URI_LIST, arrayListOf(params.iconUri))
						.putExtra(FileUploadService.EXTRA_PATH, ICON_DIR_PATH)
						.putExtra(ChatIconUploadService.EXTRA_CHAT_ID, chatId)
						.putExtra(ChatIconUploadService.EXTRA_USER_ID, params.userId)
						.putExtra(ChatIconUploadService.EXTRA_CHAT_TYPE, params.chatType)
						.putStringArrayListExtra(ChatIconUploadService.EXTRA_CHAT_PARTICIPANTS,
							if (params.participants is ArrayList)
								params.participants as ArrayList
							else ArrayList(params.participants))
						.setAction(FileUploadService.ACTION_UPLOAD))
				}
			}
		}
	}
}
