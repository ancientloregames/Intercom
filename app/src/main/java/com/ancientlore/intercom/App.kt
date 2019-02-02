package com.ancientlore.intercom

import androidx.multidex.MultiDexApplication
import com.ancientlore.intercom.backend.firebase.FirebaseFactory

class App : MultiDexApplication() {

	companion object {

		val backend = FirebaseFactory
	}
}