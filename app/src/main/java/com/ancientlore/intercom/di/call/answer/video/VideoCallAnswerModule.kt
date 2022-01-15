package com.ancientlore.intercom.di.call.answer.video

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.video.VideoCallAnswerFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class VideoCallAnswerModule {

	@VideoCallAnswerScreenScope
	@Provides
	@Named("VideoCallAnswer")
	fun getParams(fragment: VideoCallAnswerFragment): CallAnswerParams {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}