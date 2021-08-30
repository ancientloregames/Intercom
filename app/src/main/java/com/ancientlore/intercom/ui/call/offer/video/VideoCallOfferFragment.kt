package com.ancientlore.intercom.ui.call.offer.video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallVideoOfferUiBinding
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.CallOfferFragment
import kotlinx.android.synthetic.main.call_video_offer_ui.*
import java.lang.RuntimeException

class VideoCallOfferFragment
	: CallOfferFragment<VideoCallOfferViewModel, CallVideoOfferUiBinding>() {

	companion object {

		const val ARG_PARAMS = "params"

		fun newInstance(params: CallViewModel.Params) : VideoCallOfferFragment {
			return VideoCallOfferFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : CallViewModel.Params by lazy {
		arguments?.getParcelable<CallViewModel.Params>(ARG_PARAMS)
			?: throw RuntimeException("Params are a mandotory arg") }

	private var hudAnimationDuration: Long = 200

	override fun getInfoPanelView(): View = callInfoPanel

	override fun getControlPanelView(): View = callControlPanel

	override fun getChronometer(): Chronometer = chronometer

	override fun getLayoutResId() = R.layout.call_video_offer_ui

	override fun createViewModel() = VideoCallOfferViewModel(params)

	override fun onAttach(context: Context) {
		super.onAttach(context)

		hudAnimationDuration = context.resources.getInteger(R.integer.defaultShowHideAnimationDuration).toLong()
	}

	override fun bind(view: View, viewModel: VideoCallOfferViewModel) {
		dataBinding = CallVideoOfferUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: VideoCallOfferViewModel) {
		super.initViewModel(viewModel)

		audioManager.isSpeakerphoneOn = true

		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			call(CallManager.CallParams(
				params.targetId,
				localVideoRenderer,
				remoteVideoRenderer
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