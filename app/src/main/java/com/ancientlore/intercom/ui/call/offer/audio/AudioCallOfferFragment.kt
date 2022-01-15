package com.ancientlore.intercom.ui.call.offer.audio

import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.CallAudioOfferUiBinding
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.CallOfferFragment
import javax.inject.Inject
import javax.inject.Named

class AudioCallOfferFragment
	: CallOfferFragment<AudioCallOfferViewModel, CallAudioOfferUiBinding>() {

	companion object {

		fun newInstance(params: CallViewModel.Params) : AudioCallOfferFragment {
			return AudioCallOfferFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	@Inject
	@Named("AudioCallOffer")
	protected lateinit var params: CallViewModel.Params

	@Inject
	protected lateinit var viewModel: AudioCallOfferViewModel

	override fun getInfoPanelView(): View = dataBinding.callInfoPanel

	override fun getControlPanelView(): View = dataBinding.callControlPanel

	override fun getChronometer(): Chronometer = dataBinding.chronometer

	override fun getLayoutResId() = R.layout.call_audio_offer_ui

	override fun createDataBinding(view: View) = CallAudioOfferUiBinding.bind(view)

	override fun requestViewModel(): AudioCallOfferViewModel = viewModel

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