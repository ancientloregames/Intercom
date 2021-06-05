package com.ancientlore.intercom.ui.dialog.option.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatOptionMenuUiBinding
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.internal.disposables.ListCompositeDisposable

class ChatOptionMenuDialog: DialogFragment() {

	companion object {
		const val ARG_PARAMS = "arg_params"

		fun newInstance(params: ChatOptionMenuParams): ChatOptionMenuDialog {
			return ChatOptionMenuDialog().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : ChatOptionMenuParams by lazy { arguments?.getParcelable<ChatOptionMenuParams>(
		ARG_PARAMS
	)
		?: throw RuntimeException("Params are a mandotory arg") }

	interface Listener {
		fun onPinClicked(pin: Boolean)
		fun onMuteClicked(pin: Boolean)
	}
	var listener: Listener? = null

	private lateinit var viewModel: ChatOptionMenuViewModel

	private val subscriptions = ListCompositeDisposable()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.chat_option_menu_ui, container)

		setupView()

		setupViewModel(view)

		subscribeOnViewModel()

		return view
	}

	override fun show(manager: FragmentManager, tag: String?) {
		val transaction = manager.beginTransaction()
		manager.findFragmentByTag(tag)
			?.let { transaction.remove(it) }
		transaction.addToBackStack(null)

		runOnUiThread {
			show(transaction, tag)
		}
	}

	private fun setupView() {
		setStyle(STYLE_NO_FRAME, android.R.style.Theme)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
	}

	private fun setupViewModel(view: View) {
		val binder = ChatOptionMenuUiBinding.bind(view)
		viewModel = ChatOptionMenuViewModel(params)
		binder.ui = viewModel
	}

	override fun onDestroyView() {
		viewModel.clean()
		subscriptions.dispose()
		listener = null

		super.onDestroyView()
	}

	private fun subscribeOnViewModel() {
		subscriptions.add(viewModel.observePinClicked()
			.subscribe {
				listener?.onPinClicked(it)
				dismiss()
			})

		subscriptions.add(viewModel.observeMuteClicked()
			.subscribe {
				listener?.onMuteClicked(it)
				dismiss()
			})
	}
}