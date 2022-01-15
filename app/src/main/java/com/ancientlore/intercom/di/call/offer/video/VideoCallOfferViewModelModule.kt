package com.ancientlore.intercom.di.call.offer.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.call.offer.video.VideoCallOfferViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface VideoCallOfferViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@VideoCallOfferScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(VideoCallOfferViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: VideoCallOfferViewModel): ViewModel
}