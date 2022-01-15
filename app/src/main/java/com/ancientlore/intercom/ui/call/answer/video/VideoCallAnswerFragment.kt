package com.ancientlore.intercom.ui.call.answer.video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallVideoAnswerUiBinding
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerFragment
import javax.inject.Inject
import javax.inject.Named

class VideoCallAnswerFragment
	: CallAnswerFragment<VideoCallAnswerViewModel, CallVideoAnswerUiBinding>() {

	companion object {

		fun newInstance(params: CallAnswerParams) : VideoCallAnswerFragment {
			return VideoCallAnswerFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	@Inject
	@Named("VideoCallAnswer")
	protected lateinit var params: CallAnswerParams

	@Inject
	protected lateinit var viewModel: VideoCallAnswerViewModel

	private var hudAnimationDuration: Long = 200

	override fun getInfoPanelView(): View = dataBinding.callInfoPanel

	override fun getControlPanelView(): View = dataBinding.callControlPanel

	override fun getChronometer(): Chronometer = dataBinding.chronometer

	override fun getLayoutResId(): Int = R.layout.call_video_answer_ui

	override fun onAttach(context: Context) {
		super.onAttach(context)

		hudAnimationDuration = context.resources.getInteger(R.integer.defaultShowHideAnimationDuration).toLong()
	}

	override fun createDataBinding(view: View) = CallVideoAnswerUiBinding.bind(view)

	override fun requestViewModel(): VideoCallAnswerViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		audioManager.isSpeakerphoneOn = true

		subscriptions.addAll(
			viewModel.showHUDRequest().subscribe { animateHud(it) },

			viewModel.makeCallRequest().subscribe { answer() }
			)
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

	// FIXME ViewModel should communicate with backand, buy sholdn't know about videoViews. This need refactoring
	private fun answer() {

		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			answer(
				CallManager.CallParams(
					viewModel.params.targetId,
					dataBinding.localVideoRenderer,
					dataBinding.remoteVideoRenderer),
				(viewModel.params as CallAnswerParams).sdp)
		}
	}

	override fun endCall() {
		audioManager.isSpeakerphoneOn = false
		super.endCall()
	}
}