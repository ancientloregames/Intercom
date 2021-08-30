package com.ancientlore.intercom.ui.call

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import androidx.annotation.CallSuper
import androidx.annotation.RawRes
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.App
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.Utils
import java.lang.Exception

abstract class CallFragment<VM : CallViewModel, B : ViewDataBinding>  : BasicFragment<VM, B>() {

	companion object {
		private const val proxSensorLevelFieldName = "PROXIMITY_SCREEN_OFF_WAKE_LOCK"
		private const val proxSensorLevelFallback = 0x00000020
	}

	protected val userId = App.backend.getAuthManager().getCurrentUser().id

	protected lateinit var audioManager: AudioManager

	private lateinit var wakeLock: PowerManager.WakeLock

	private lateinit var mediaPlayer: MediaPlayer

	abstract fun getInfoPanelView(): View

	abstract fun getControlPanelView(): View

	abstract fun getChronometer(): Chronometer

	@RawRes
	abstract fun getCallSound(): Int

	override fun onBackPressed(): Boolean {
		endCall()
		return true
	}

	@CallSuper
	override fun onAttach(context: Context) {
		super.onAttach(context)

		audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

		mediaPlayer = MediaPlayer.create(activity, getCallSound())

		// Prior to API21 this field was private
		val field = try {
			PowerManager::class.java.getField(proxSensorLevelFieldName).getInt(null)
		} catch (e: Exception) {
			Utils.logError(e)
			proxSensorLevelFallback
		}
		wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
			.newWakeLock(field, this.javaClass.name)
	}

	@CallSuper
	override fun initViewModel(viewModel: VM) {

		mediaPlayer.run {
			isLooping = true
			try {
				start()
			} catch (e: IllegalStateException) {
				Utils.logError(e)
			}
		}

		subscriptions.add(viewModel.startChronometerRequest()
			.subscribe { startChronometer() })
		subscriptions.add(viewModel.hangupCallRequest()
			.subscribe { endCall() })
	}

	private fun startChronometer() {

		runOnUiThread {
			getChronometer().run {
				base = SystemClock.elapsedRealtime()
				start()
				visibility = View.VISIBLE
			}
		}
	}

	protected fun turnOnProximitySensor() {
		if (!wakeLock.isHeld)
			wakeLock.acquire()
	}

	protected fun turnOffProximitySensor() {
		if (wakeLock.isHeld)
			wakeLock.release()
	}

	@CallSuper
	protected open fun endCall() {

		stopCallSound()

		getChronometer().stop()

		App.backend.getCallManager().hungup()
		close()
	}

	protected fun stopCallSound() {
		try {
			mediaPlayer.run {
				if (isPlaying)
					stop()
				reset()
				release()
			}
		} catch (e: IllegalStateException) {
			Utils.logError(e)
		}
	}
}