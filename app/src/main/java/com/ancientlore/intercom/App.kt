package com.ancientlore.intercom

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.ancientlore.intercom.backend.firebase.FirebaseFactory

class App : MultiDexApplication() {

	companion object {

		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}

		val backend = FirebaseFactory
	}
}