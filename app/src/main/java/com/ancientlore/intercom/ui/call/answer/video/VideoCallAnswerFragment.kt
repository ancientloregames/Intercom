package com.ancientlore.intercom.ui.call.answer.video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallVideoAnswerUiBinding
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerFragment
import kotlinx.android.synthetic.main.call_video_answer_ui.*
import java.lang.RuntimeException

class VideoCallAnswerFragment
	: CallAnswerFragment<VideoCallAnswerViewModel, CallVideoAnswerUiBinding>() {

	companion object {

		const val ARG_PARAMS = "params"

		fun newInstance(params: CallAnswerParams) : VideoCallAnswerFragment {
			return VideoCallAnswerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : CallAnswerParams by lazy {
		arguments?.getParcelable<CallAnswerParams>(ARG_PARAMS)
			?: throw RuntimeException("Params are a mandotory arg") }

	private var hudAnimationDuration: Long = 200

	override fun getInfoPanelView(): View = callInfoPanel

	override fun getControlPanelView(): View = callControlPanel

	override fun getChronometer(): Chronometer = chronometer

	override fun getLayoutResId(): Int = R.layout.call_video_answer_ui

	override fun createViewModel() = VideoCallAnswerViewModel(params)

	override fun onAttach(context: Context) {
		super.onAttach(context)

		hudAnimationDuration = context.resources.getInteger(R.integer.defaultShowHideAnimationDuration).toLong()
	}

	override fun bind(view: View, viewModel: VideoCallAnswerViewModel) {
		dataBinding = CallVideoAnswerUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: VideoCallAnswerViewModel) {
		super.initViewModel(viewModel)

		audioManager.isSpeakerphoneOn = true

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

	override fun answer() {
		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			answer(
				CallManager.CallParams(
				params.targetId,
				localVideoRenderer,
				remoteVideoRenderer), params.sdp)
		}
	}

	override fun endCall() {
		audioManager.isSpeakerphoneOn = false
		super.endCall()
	}
}