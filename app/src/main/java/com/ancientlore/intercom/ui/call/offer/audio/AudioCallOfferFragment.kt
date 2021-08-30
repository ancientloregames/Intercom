package com.ancientlore.intercom.ui.call.offer.audio

import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallAudioOfferUiBinding
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.CallOfferFragment
import kotlinx.android.synthetic.main.call_audio_offer_ui.*
import java.lang.RuntimeException

class AudioCallOfferFragment
	: CallOfferFragment<AudioCallOfferViewModel, CallAudioOfferUiBinding>() {

	companion object {

		const val ARG_PARAMS = "params"

		fun newInstance(params: CallViewModel.Params) : AudioCallOfferFragment {
			return AudioCallOfferFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : CallViewModel.Params by lazy {
		arguments?.getParcelable<CallViewModel.Params>(ARG_PARAMS)
			?: throw RuntimeException("Params are a mandotory arg") }

	override fun getInfoPanelView(): View = callInfoPanel

	override fun getControlPanelView(): View = callControlPanel

	override fun getChronometer(): Chronometer = chronometer

	override fun getLayoutResId() = R.layout.call_audio_offer_ui

	override fun createViewModel() = AudioCallOfferViewModel(params)

	override fun bind(view: View, viewModel: AudioCallOfferViewModel) {
		dataBinding = CallAudioOfferUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: AudioCallOfferViewModel) {
		super.initViewModel(viewModel)

		subscriptions.add(viewModel.turnOnProximitySensorRequest()
			.subscribe { turnOnProximitySensor() })

		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			call(
				CallManager.AudioCallParams(
					params.targetId
				))
		}
	}

	override fun endCall() {
		turnOffProximitySensor()
		super.endCall()
	}
}