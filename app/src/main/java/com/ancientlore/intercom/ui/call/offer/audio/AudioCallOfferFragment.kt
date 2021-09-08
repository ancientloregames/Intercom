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

	override fun getInfoPanelView(): View = dataBinding.callInfoPanel

	override fun getControlPanelView(): View = dataBinding.callControlPanel

	override fun getChronometer(): Chronometer = dataBinding.chronometer

	override fun getLayoutResId() = R.layout.call_audio_offer_ui

	override fun createDataBinding(view: View) = CallAudioOfferUiBinding.bind(view)

	override fun createViewModel() = AudioCallOfferViewModel(params)

	override fun init(viewModel: AudioCallOfferViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

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