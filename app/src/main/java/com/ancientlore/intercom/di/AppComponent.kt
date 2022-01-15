package com.ancientlore.intercom.di

import android.content.Context
import com.ancientlore.intercom.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
	AndroidSupportInjectionModule::class,
	ActivityBuildersModule::class,
	FragmentBuildersModule::class
])
interface AppComponent: AndroidInjector<App> {

	@Component.Factory
	interface Factory {
		fun create(@BindsInstance appContext: Context): AppComponent
	}
}