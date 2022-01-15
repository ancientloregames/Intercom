package com.ancientlore.intercom.di.call.answer.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.call.answer.audio.AudioCallAnswerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AudioCallAnswerViewModelModule {

	@AudioCallAnswerScreenScope
	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@AudioCallAnswerScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(AudioCallAnswerViewModel::class)
	fun bindAudioCallAnswerViewModel(viewModel: AudioCallAnswerViewModel): ViewModel
}