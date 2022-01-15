package com.ancientlore.intercom.di

import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.view.LayoutInflater
import com.ancientlore.intercom.MainActivity
import dagger.Module
import dagger.Provides

@Module
class ActivityModule {

	@MainActivityScope
	@Provides
	fun layoutInflater(activity: MainActivity): LayoutInflater {
		return (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
	}

	@MainActivityScope
	@Provides
	fun audioManager(activity: MainActivity): AudioManager {
		return (activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
	}

	@MainActivityScope
	@Provides
	fun powerManager(activity: MainActivity): PowerManager {
		return (activity.getSystemService(Context.POWER_SERVICE) as PowerManager)
	}
}