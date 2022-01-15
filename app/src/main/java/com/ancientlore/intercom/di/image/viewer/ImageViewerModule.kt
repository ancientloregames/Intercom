package com.ancientlore.intercom.di.image.viewer

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.image.viewer.ImageViewerFragment
import com.ancientlore.intercom.ui.image.viewer.ImageViewerViewModel
import dagger.Module
import dagger.Provides

@Module
class ImageViewerModule {

	@ImageViewerScreenScope
	@Provides
	fun getParams(fragment: ImageViewerFragment): ImageViewerViewModel.Params {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}