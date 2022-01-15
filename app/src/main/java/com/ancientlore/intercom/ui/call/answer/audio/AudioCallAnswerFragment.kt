package com.ancientlore.intercom.ui.call.answer.audio

import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.CallAudioAnswerUiBinding
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerFragment
import javax.inject.Inject
import javax.inject.Named

class AudioCallAnswerFragment
	: CallAnswerFragment<AudioCallAnswerViewModel, CallAudioAnswerUiBinding>() {

	companion object {

		fun newInstance(params: CallAnswerParams): AudioCallAnswerFragment {
			return AudioCallAnswerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	@Inject
	@Named("AudioCallAnswer")
	protected lateinit var params: CallAnswerParams

	@Inject
	protected lateinit var viewModel: AudioCallAnswerViewModel

	override fun getInfoPanelView(): View = dataBinding.callInfoPanel

	override fun getControlPanelView(): View = dataBinding.callControlPanel

	override fun getChronometer(): Chronometer = dataBinding.chronometer

	override fun getLayoutResId(): Int = R.layout.call_audio_answer_ui

	override fun createDataBinding(view: View) = CallAudioAnswerUiBinding.bind(view)

	override fun requestViewModel(): AudioCallAnswerViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		subscriptions.add(viewModel.turnOnProximitySensorRequest()
			.subscribe { turnOnProximitySensor() })
	}

	override fun endCall() {
		turnOffProximitySensor()
		super.endCall()
	}
}