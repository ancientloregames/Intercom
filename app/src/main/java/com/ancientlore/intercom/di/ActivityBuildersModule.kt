package com.ancientlore.intercom.di

import com.ancientlore.intercom.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityBuildersModule {

	@MainActivityScope
	@ContributesAndroidInjector(
		modules = [ActivityModule::class]
	)
	fun contributeMainActivity(): MainActivity
}