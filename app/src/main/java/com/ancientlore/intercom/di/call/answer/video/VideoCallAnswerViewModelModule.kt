package com.ancientlore.intercom.di.call.answer.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.call.answer.video.VideoCallAnswerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface VideoCallAnswerViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@VideoCallAnswerScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(VideoCallAnswerViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: VideoCallAnswerViewModel): ViewModel
}