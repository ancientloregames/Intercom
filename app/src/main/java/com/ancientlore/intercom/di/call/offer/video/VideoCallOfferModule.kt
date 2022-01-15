package com.ancientlore.intercom.di.call.offer.video

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.offer.video.VideoCallOfferFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class VideoCallOfferModule {

	@VideoCallOfferScreenScope
	@Provides
	@Named("VideoCallOffer")
	fun getParams(fragment: VideoCallOfferFragment): CallViewModel.Params {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}