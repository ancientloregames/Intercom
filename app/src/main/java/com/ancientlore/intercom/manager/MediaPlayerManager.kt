package com.ancientlore.intercom.manager

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.annotation.IntDef
import com.ancientlore.intercom.utils.Utils

object MediaPlayerManager : MediaPlayer.OnCompletionListener {

	const val STATUS_RELEASED = 0
	const val STATUS_READY = 1
	const val STATUS_PLAYING = 2
	const val STATUS_PAUSED = 3

	@IntDef(STATUS_RELEASED, STATUS_READY, STATUS_PLAYING, STATUS_PAUSED)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Status

	interface Listener {
		fun onComplete()
		fun onProgress(progress: Int)
	}

	private var listener: Listener? = null

	@Status @Volatile
	private var status = STATUS_RELEASED

	private var player: MediaPlayer? = null

	private val progressHandler = Handler(Looper.getMainLooper())
	private val progressTask = object : Runnable {
		override fun run() {
			player?.takeIf { it.isPlaying }
				?.let { listener?.onProgress(it.currentPosition) }
			progressHandler.postDelayed(this, 100)
		}
	}

	override fun onCompletion(mp: MediaPlayer?) {
		progressHandler.removeCallbacks(progressTask)
		release()
		listener?.onComplete()
	}

	fun prepare(filePath: String) {
		release()
		try {
			player = MediaPlayer().apply {
				setOnCompletionListener(this@MediaPlayerManager)
				setDataSource(filePath)
				prepare()
				status = STATUS_READY
			}
		} catch (e: IllegalStateException) {
			Utils.logError(e)
		}
	}

	fun pause() {
		try {
			player?.pause()
			status = STATUS_PAUSED
		} catch (e: IllegalStateException) {
			Utils.logError(e)
		}
		progressHandler.removeCallbacks(progressTask)
	}

	fun play() {
		try {
			player?.start()
			status = STATUS_PLAYING
			progressHandler.post(progressTask)
		} catch (e: IllegalStateException) {
			Utils.logError(e)
		}
	}

	fun release() {
		player?.run {
			try {
				stop()
				release()
				status = STATUS_RELEASED
			} catch (e: IllegalStateException) {
				Utils.logError(e)
			}
		}
		player = null
	}

	fun getStatus() = status

	fun setProgress(progress: Int) {
		try {
			player?.seekTo(progress)
		} catch (e: IllegalStateException) {
			Utils.logError(e)
		}
	}

	fun setListener(listener: Listener) {
		this.listener = listener
	}
}