package com.ancientlore.intercom

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.ancientlore.intercom.backend.firebase.FirebaseFactory
import com.ancientlore.intercom.di.DaggerAppComponent
import com.ancientlore.intercom.frontend.FrontendFactory
import com.ancientlore.intercom.frontend.IntercomFrontendFactory
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class App : DaggerApplication() {

	companion object {

		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}

		val backend = FirebaseFactory

		lateinit var frontend: FrontendFactory
			private set

		@SuppressLint("StaticFieldLeak")
		lateinit var context: Context
	}

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)

		MultiDex.install(this)
	}

	override fun onCreate() {
		try {
			context = applicationContext
			frontend = IntercomFrontendFactory(context)
		} catch (ignore: Throwable) { }

		super.onCreate()

		if (context == null) {
			context = applicationContext
			frontend = IntercomFrontendFactory(context)
		}
	}

	override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
		return DaggerAppComponent.factory().create(this)
	}
}