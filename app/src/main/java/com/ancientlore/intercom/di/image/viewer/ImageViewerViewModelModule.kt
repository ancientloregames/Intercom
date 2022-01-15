package com.ancientlore.intercom.di.image.viewer

import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.image.viewer.ImageViewerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ImageViewerViewModelModule {

	@ImageViewerScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ImageViewerViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ImageViewerViewModel): ViewModel
}