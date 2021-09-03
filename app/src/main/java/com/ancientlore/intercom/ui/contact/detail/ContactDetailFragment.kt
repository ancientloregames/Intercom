package com.ancientlore.intercom.ui.contact.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ContactDetailUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailViewModel.Companion.OPTION_VIDEO_CALL
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.extensions.putToClipboard
import kotlinx.android.synthetic.main.contact_detail_ui.*

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

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId(): Int = R.layout.contact_detail_ui

	override fun createViewModel() = ContactDetailViewModel(params)

	override fun bind(view: View, viewModel: ContactDetailViewModel) {
		dataBinding = ContactDetailUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		navigator?.run {
			createToolbarMenu(toolbar) { menu ->
				activity?.menuInflater?.inflate(R.menu.contact_detail_menu, menu)
			}

			ToolbarManager(toolbar as Toolbar).apply {
				enableBackButton { close() }
			}

			setHasOptionsMenu(true)
		}

		swipableLayout.setListener { close(false) }
	}

	override fun initViewModel(viewModel: ContactDetailViewModel) {
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
		subscriptions.add(viewModel.observeCloseRequest()
			.subscribe {
				close()
			})
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.audioCall -> {
				viewModel.onOptionSelected(OPTION_VIDEO_CALL)
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