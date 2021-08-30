package com.ancientlore.intercom.ui.call.answer.audio

import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallAudioAnswerUiBinding
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerFragment
import kotlinx.android.synthetic.main.call_audio_answer_ui.*
import java.lang.RuntimeException

class AudioCallAnswerFragment
	: CallAnswerFragment<AudioCallAnswerViewModel, CallAudioAnswerUiBinding>() {

	companion object {

		const val ARG_PARAMS = "params"

		fun newInstance(params: CallAnswerParams) : AudioCallAnswerFragment {
			return AudioCallAnswerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : CallAnswerParams by lazy {
		arguments?.getParcelable<CallAnswerParams>(ARG_PARAMS)
			?: throw RuntimeException("Params are a mandotory arg") }

	override fun getInfoPanelView(): View = callInfoPanel

	override fun getControlPanelView(): View = callControlPanel

	override fun getChronometer(): Chronometer = chronometer

	override fun getLayoutResId(): Int = R.layout.call_audio_answer_ui

	override fun createViewModel() = AudioCallAnswerViewModel(params)

	override fun bind(view: View, viewModel: AudioCallAnswerViewModel) {
		dataBinding = CallAudioAnswerUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: AudioCallAnswerViewModel) {
		super.initViewModel(viewModel)

		subscriptions.add(viewModel.turnOnProximitySensorRequest()
			.subscribe { turnOnProximitySensor() })
	}

	override fun answer() {
		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			answer(
				CallManager.AudioCallParams(
					params.targetId), params.sdp)
		}
	}

	override fun endCall() {
		turnOffProximitySensor()
		super.endCall()
	}
}