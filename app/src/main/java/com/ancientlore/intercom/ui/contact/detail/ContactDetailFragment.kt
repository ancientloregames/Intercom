package com.ancientlore.intercom.ui.contact.detail

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactDetailUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailViewModel.Companion.OPTION_AUDIO_CALL
import com.ancientlore.intercom.ui.contact.detail.ContactDetailViewModel.Companion.OPTION_VIDEO_CALL
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.extensions.putToClipboard

class ContactDetailFragment : BasicFragment<ContactDetailViewModel, ContactDetailUiBinding>() {

	companion object {
		private const val ARG_PARAMS = "params"

		fun newInstance(params: ContactDetailParams) : ContactDetailFragment {
			return ContactDetailFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : ContactDetailParams by lazy { arguments?.getParcelable<ContactDetailParams>(ARG_PARAMS)
		?: throw RuntimeException("Contact params are a mandotory arg") }

	override fun getLayoutResId(): Int = R.layout.contact_detail_ui

	override fun createDataBinding(view: View) = ContactDetailUiBinding.bind(view)

	override fun createViewModel() = ContactDetailViewModel(params)

	override fun init(viewModel: ContactDetailViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		navigator?.run {
			createToolbarMenu(dataBinding.toolbar) { menu ->
				activity?.menuInflater?.inflate(R.menu.contact_detail_menu, menu)
			}

			ToolbarManager(dataBinding.toolbar).apply {
				enableBackButton { close() }
			}

			setHasOptionsMenu(true)
		}

		dataBinding.swipableLayout.setListener { close(false) }

		subscriptions.add(viewModel.observePutToClipboardRequest()
			.subscribe {
				context?.run {
					putToClipboard(it)
					showToast(R.string.text_copied, Toast.LENGTH_SHORT)
				}
			})
		subscriptions.add(viewModel.observeOpenChatFlowRequest()
			.subscribe {
				navigator?.openChatFlow(it)
			})
		subscriptions.add(viewModel.observeMakeAudioCallRequest()
			.subscribe {
				navigator?.openAudioCallOffer(it)
			})
		subscriptions.add(viewModel.observeMakeVideoCallRequest()
			.subscribe {
				navigator?.openVideoCallOffer(it)
			})
		subscriptions.add(viewModel.openImageRequest()
			.subscribe {
				navigator?.openImageViewer(Uri.parse(it))
			})
		subscriptions.add(viewModel.observeCloseRequest()
			.subscribe {
				close()
			})
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
}