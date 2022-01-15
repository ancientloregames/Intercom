package com.ancientlore.intercom.di.call.offer.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.call.offer.audio.AudioCallOfferViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AudioCallOfferViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@AudioCallOfferScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(AudioCallOfferViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: AudioCallOfferViewModel): ViewModel
}