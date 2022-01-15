package com.ancientlore.intercom.ui.call.offer.video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallVideoOfferUiBinding
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.CallOfferFragment
import javax.inject.Inject
import javax.inject.Named

class VideoCallOfferFragment
	: CallOfferFragment<VideoCallOfferViewModel, CallVideoOfferUiBinding>() {

	companion object {

		fun newInstance(params: CallViewModel.Params) : VideoCallOfferFragment {
			return VideoCallOfferFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	@Inject
	@Named("VideoCallOffer")
	protected lateinit var params: CallViewModel.Params

	@Inject
	protected lateinit var viewModel: VideoCallOfferViewModel

	private var hudAnimationDuration: Long = 200

	override fun getInfoPanelView(): View = dataBinding.callInfoPanel

	override fun getControlPanelView(): View = dataBinding.callControlPanel

	override fun getChronometer(): Chronometer = dataBinding.chronometer

	override fun getLayoutResId() = R.layout.call_video_offer_ui

	override fun onAttach(context: Context) {
		super.onAttach(context)

		hudAnimationDuration = context.resources.getInteger(R.integer.defaultShowHideAnimationDuration).toLong()
	}

	override fun createDataBinding(view: View) = CallVideoOfferUiBinding.bind(view)

	override fun requestViewModel(): VideoCallOfferViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		audioManager.isSpeakerphoneOn = true

		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			call(CallManager.CallParams(
				params.targetId,
				dataBinding.localVideoRenderer,
				dataBinding.remoteVideoRenderer
			))
		}

		subscriptions.add(viewModel.showHUDRequest()
			.subscribe { animateHud(it) })
	}

	private fun animateHud(show: Boolean) {

		runOnUiThread {
			getInfoPanelView().run {
				animate()
					.setDuration(hudAnimationDuration)
					.translationY((height * if (show) 0 else -1).toFloat())
					.start()
			}

			getControlPanelView().run {
				animate()
					.setDuration(hudAnimationDuration)
					.translationY((height * if (show) 0 else 1).toFloat())
					.start()
			}
		}
	}

	override fun endCall() {
		audioManager.isSpeakerphoneOn = false
		super.endCall()
	}
}