package com.ancientlore.intercom

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.ancientlore.intercom.backend.firebase.FirebaseFactory

class App : MultiDexApplication() {

	companion object {

		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}

		val backend = FirebaseFactory

		@SuppressLint("StaticFieldLeak")
		lateinit var context: Context
	}

	override fun onCreate() {
		try {
			context = applicationContext
		} catch (ignore: Throwable) { }

		super.onCreate()

		if (context == null)
			context = applicationContext
	}
}