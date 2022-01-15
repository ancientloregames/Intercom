package com.ancientlore.intercom.di.call.answer.audio

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.audio.AudioCallAnswerFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class AudioCallAnswerModule {

	@AudioCallAnswerScreenScope
	@Provides
	@Named("AudioCallAnswer")
	fun getParams(fragment: AudioCallAnswerFragment): CallAnswerParams {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}