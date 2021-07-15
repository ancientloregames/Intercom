package com.ancientlore.intercom

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.ancientlore.intercom.backend.firebase.FirebaseFactory
import com.ancientlore.intercom.frontend.FrontendFactory
import com.ancientlore.intercom.frontend.IntercomFrontendFactory

class App : MultiDexApplication() {

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
}