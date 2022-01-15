package com.ancientlore.intercom.di.call.offer.audio

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.audio.AudioCallOfferFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class AudioCallOfferModule {

	@AudioCallOfferScreenScope
	@Provides
	@Named("AudioCallOffer")
	fun getParams(fragment: AudioCallOfferFragment): CallViewModel.Params {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}