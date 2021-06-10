package com.ancientlore.intercom.ui.chat.flow

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ancientlore.intercom.C
import com.ancientlore.intercom.C.ICON_DIR_PATH
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatFlowUiBinding
import com.ancientlore.intercom.dialog.bottomsheet.list.ListBottomSheetDialog
import com.ancientlore.intercom.service.ChatIconUploadService
import com.ancientlore.intercom.service.FileUploadService
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.dialog.attach.AttachBottomSheetDialog
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.enableChatBehavior
import com.ancientlore.intercom.utils.extensions.getAppCacheDir
import com.ancientlore.intercom.utils.extensions.getFileData
import com.ancientlore.intercom.utils.extensions.openFile
import com.ancientlore.intercom.view.MessageInputManager
import com.ancientlore.intercom.widget.list.simple.SimpleListItem
import kotlinx.android.synthetic.main.chat_flow_ui.*
import kotlinx.android.synthetic.main.chat_flow_ui.listView
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

	override fun getToolbarMenuResId() = R.menu.chat_flow_menu

	override fun getLayoutResId() = R.layout.chat_flow_ui

	override fun createViewModel() = ChatFlowViewModel(listView.adapter as ChatFlowAdapter, params)

	override fun bind(view: View, viewModel: ChatFlowViewModel) {
		dataBinding = ChatFlowUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			//setTitle(params.title)
			enableBackButton { close() }
			//setLogo(params.iconUri, getFallbackActionBarIcon())
		}

		swipableLayout.setListener { close() }

		with(listView) {
			adapter = ChatFlowAdapter(params.userId, requireContext())
			enableChatBehavior()
		}
	}

	private fun getFallbackActionBarIcon() : Drawable {

		return if (params.title.isNotEmpty()) {
			ImageUtils.createAbbreviationDrawable(toolbar.title.toString(),
				ContextCompat.getColor(toolbar.context, R.color.chatIconBackColor),
				toolbar.resources.getDimensionPixelSize(R.dimen.chatListIconTextSize))
		}
		else throw RuntimeException("Error! Chat flow title is mandatory")
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

		if (permissionManager!!.allowedAudioMessage())
			viewModel.attachInputPanelManager(MessageInputManager(view!!))
	}

	override fun observeViewModel(viewModel: ChatFlowViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeAttachMenuOpen()
			.subscribe { openAttachMenu() })
		subscriptions.add(viewModel.observeAudioRecord()
			.subscribe { recordAudio() })
		subscriptions.add(viewModel.observeUploadIcon()
			.subscribe { uploadIcon(it) })
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
