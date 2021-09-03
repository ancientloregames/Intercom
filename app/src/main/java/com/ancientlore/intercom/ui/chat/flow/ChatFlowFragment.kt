package com.ancientlore.intercom.ui.chat.flow

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.chat_flow_ui.textInput
import kotlinx.android.synthetic.main.chat_flow_ui.listView
import kotlinx.android.synthetic.main.chat_flow_ui.swipableLayout
import kotlinx.android.synthetic.main.chat_flow_ui.toolbar
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

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getToolbar(): Toolbar = toolbar

	override fun getToolbarMenuResId(): Int {
		return if (params.chatType == Chat.TYPE_PRIVATE) // TODO really need to separate entities
			R.menu.chat_flow_menu
		else R.menu.chat_flow_group_menu
	}

	override fun getLayoutResId() = R.layout.chat_flow_ui

	override fun createViewModel() = ChatFlowViewModel(listView.adapter as ChatFlowAdapter, params)

	override fun bind(view: View, viewModel: ChatFlowViewModel) {
		dataBinding = ChatFlowUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		setHasOptionsMenu(true)

		ToolbarManager(toolbar as Toolbar).apply {
			//setTitle(params.title)
			enableBackButton { close() }
			//setLogo(params.iconUri, getFallbackActionBarIcon())
		}

		swipableLayout.setListener { close(false) }

		with(listView) {
			adapter = ChatFlowAdapter(params.userId, requireContext())

			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
					super.onScrollStateChanged(recyclerView, newState)
					if (!recyclerView.canScrollVertically(-1)
						&& recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
						&& adapter!!.itemCount > 1) {
						viewModel.onScrolledToTop()
					}
				}
			})

			enableChatBehavior()
		}

		Utils.runOnUiThread({ textInput.showKeyboard() }, 200)

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

	override fun initViewModel(viewModel: ChatFlowViewModel) {
		context?. let {
			viewModel.init(it)
		}

		val listAdapter = listView.adapter as ChatFlowAdapter
		subscriptions.add(listAdapter.observeFileOpen()
			.subscribe {
				context?.openFile(it)
			})
		subscriptions.add(listAdapter.observeImageOpen()
			.subscribe {
				// TODO create custom image viewer fragment
				context?.openFile(it)
			})
		subscriptions.add(listAdapter.observeOptionMenuOpen()
			.subscribe {
				openMessageMenu(it)
			})
		subscriptions.add(viewModel.observeMakeAudioCallRequest()
			.subscribe {
				navigator?.openAudioCallOffer(it)
			})
		subscriptions.add(viewModel.observeMakeVideoCallRequest()
			.subscribe {
				navigator?.openVideoCallOffer(it)
			})

		if (permissionManager!!.allowedAudioMessage())
			viewModel.attachInputPanelManager(MessageInputManager(view!!))
	}

	private fun openMessageMenu(message: Message) {
		activity?.run {

			val dialog = MessageOptionMenuDialog.newInstance()

			dialog.listener = object : MessageOptionMenuDialog.Listener {
				override fun onDeleteClicked() {
					viewModel.handleDelete(message)
				}
			}

			dialog.show(supportFragmentManager)
		}
	}

	override fun observeViewModel(viewModel: ChatFlowViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeAttachMenuOpen()
			.subscribe { openAttachMenu() })
		subscriptions.add(viewModel.observeAudioRecord()
			.subscribe { recordAudio() })
		subscriptions.add(viewModel.observeUploadIcon()
			.subscribe { uploadIcon(it) })
		subscriptions.add(viewModel.observeOpenChatDetail()
			.subscribe { navigator?.openChatDetail(params) })
		subscriptions.add(viewModel.observeOpenContactDetail()
			.subscribe { navigator?.openContactDetail(it) })
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

	private fun openAttachMenu() {
		AttachBottomSheetDialog.newInstance().apply {
			setListener(object : ListBottomSheetDialog.Listener {
				override fun onItemSelected(item: SimpleListItem) {
					onAttachMenuItemSelected(item.id)
				}
			})
		}.show(fragmentManager!!)
	}

	private fun onAttachMenuItemSelected(id: Int) {
		when (id) {
			R.id.im_attach_picture -> openGallery()
			R.id.im_attach_file -> openFilePicker()
			else -> {
				showToast(R.string.error)
				Utils.logError(RuntimeException("Error! Unknown attach source"))
			}
		}
	}

	private fun openGallery() {
		permissionManager?.requestPermissionWriteStorage(Runnable1 { granted ->
			if (granted) {
				// FIXME temporary solution (TODO make own gallery)
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("image/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				startActivityForResult(intent, INTENT_GET_IMAGES)
			}
		})
	}

	private fun openFilePicker() {
		permissionManager?.requestPermissionReadStorage(Runnable1 { granted ->
			if (granted) {
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				startActivityForResult(intent, INTENT_GET_FILES)
			}
		})
	}

	private fun recordAudio() {
		permissionManager?.requestPermissionAudioMessage(Runnable1 { granted ->
			if (granted) {
				viewModel.attachInputPanelManager(MessageInputManager(view!!))
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
						.putExtra(FileUploadService.EXTRA_NOTIFY, true)
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
