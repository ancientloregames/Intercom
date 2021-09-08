package com.ancientlore.intercom.view

import android.annotation.SuppressLint
import android.os.SystemClock
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.Chronometer
import android.widget.EditText
import com.ancientlore.intercom.R
import com.ancientlore.intercom.widget.SimpleTextWatcher
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class MessageInputManager(view: View) {

	enum class Direction { LEFT, UP, NONE }

	interface Listener {
		fun onStarted()
		fun onCompleted()
		fun onCanceled()
	}

	private val stopButton: View = view.findViewById(R.id.stopButton)
	private val audioButton: View = view.findViewById(R.id.audioButton)
	private val sendButton: View = view.findViewById(R.id.sendButton)
	private val cancelButton: View = view.findViewById(R.id.canelButton)
	private val textInput: EditText = view.findViewById(R.id.textInput)
	private val chronometer: Chronometer = view.findViewById(R.id.chronometer)
	private val lockerContainer: View = view.findViewById(R.id.lockerContainer)
	private val attachmentButton: View = view.findViewById(R.id.attachmentButton)
	private val microphoneImage: View = view.findViewById(R.id.microphoneImage)
	private val cancelLabel: View = view.findViewById(R.id.cancelLabel)

	private var lastX = 0f
	private var lastY = 0f
	private var startX = 0f
	private var startY = 0f

	private var cancelOffset = 0f
	private var lockOffset = 0f

	private var isLocked = false
	private var isCanceled = false
	private var dontTrackMoves = false

	private var slideDirection = Direction.NONE

	private var listener: Listener? = null

	init {
		textInput.addTextChangedListener(object : SimpleTextWatcher() {
			override fun afterTextChanged(s: Editable) {
				if (s.isEmpty() && sendButton.visibility != View.GONE) {
					sendButton.visibility = View.GONE
					sendButton.animate()
						.scaleX(0f).scaleY(0f)
						.setDuration(100).setInterpolator(LinearInterpolator())
						.start()
				} else if (sendButton.visibility != View.VISIBLE && !isLocked) {
					sendButton.visibility = View.VISIBLE
					sendButton.animate()
						.scaleX(1f).scaleY(1f)
						.setDuration(100).setInterpolator(LinearInterpolator())
						.start()
				}
			}
		})

		audioButton.setOnTouchListener { button, motionEvent ->
			when (motionEvent.action) {
				MotionEvent.ACTION_DOWN -> {
					startX = motionEvent.rawX
					startY = motionEvent.rawY
					lastX = 0f
					lastY = 0f

					cancelOffset = audioButton.x / 3f
					lockOffset = audioButton.x / 2.5f

					onStart()
				}
				MotionEvent.ACTION_UP -> {
					reset()
					if (!isLocked) {
						val elapsed = SystemClock.elapsedRealtime() - chronometer.base
						if (elapsed < 1000 || isCanceled)
							onCancel()
						else onComplete()
					}
				}
				MotionEvent.ACTION_MOVE -> {
					if (dontTrackMoves)
						return@setOnTouchListener true

					var direction = Direction.NONE

					val motionX = abs(startX - motionEvent.rawX)
					val motionY = abs(startY - motionEvent.rawY)

					if (lastX < startX && lastY < startY) {
						if (motionX > motionY && lastX < startX)
							direction = Direction.LEFT
						else if (motionY > motionX && lastY < startY)
							direction = Direction.UP
					} else if (motionX > motionY && lastX < startX) {
						direction = Direction.LEFT
					} else if (motionY > motionX && lastY < startY) {
						direction = Direction.UP
					}

					if (direction == Direction.LEFT) {
						if (slideDirection == Direction.NONE || motionEvent.rawY + audioButton.width / 2 > startY)
							slideDirection = Direction.LEFT

						if (slideDirection == Direction.LEFT)
							translateX(motionEvent.rawX - startX)
					} else if (direction == Direction.UP) {
						if (slideDirection == Direction.NONE || motionEvent.rawX + audioButton.width / 2 > startX)
							slideDirection = Direction.UP

						if (slideDirection == Direction.UP)
							translateY(motionEvent.rawY - startY)
					}

					lastX = motionEvent.rawX
					lastY = motionEvent.rawY
				}
			}
			button.onTouchEvent(motionEvent)
			return@setOnTouchListener true
		}
	}

	private fun translateX(x: Float) {
		if (x > -cancelOffset) {
			audioButton.translationX = x
			cancelLabel.translationX = x
			lockerContainer.translationY = 0f
			audioButton.translationY = 0f

			if (abs(x) < microphoneImage.width / 2) {
				if (lockerContainer.visibility != View.VISIBLE)
					lockerContainer.visibility = View.VISIBLE
			} else {
				if (lockerContainer.visibility != View.GONE)
					lockerContainer.visibility = View.GONE
			}
		} else onCancel()
	}

	private fun translateY(y: Float) {
		if (y >= -lockOffset) {
			if (lockerContainer.visibility != View.VISIBLE)
				lockerContainer.visibility = View.VISIBLE

			audioButton.translationY = y
			lockerContainer.translationY = y / 2
			audioButton.translationX = 0f
		} else onLock()
	}

	fun onStop() {
		reset()
		isLocked = false
	}

	fun onCancel() {
		isCanceled = true
		reset()

		stopButton.visibility = View.GONE
		cancelButton.visibility = View.GONE

		chronometer.visibility = View.INVISIBLE
		chronometer.stop()

		microphoneImage.visibility = View.INVISIBLE
		audioButton.isEnabled = true
		switchTextInput(true)

		cancelLabel.translationX = 0f

		listener?.onCanceled()
	}

	private fun onLock() {
		isLocked = true

		reset()
		stopButton.visibility = View.VISIBLE
		cancelButton.visibility = View.VISIBLE

		audioButton.translationY = 0f
	}

	private fun onComplete() {
		reset()
		microphoneImage.visibility = View.INVISIBLE
		switchTextInput(true)
		stopButton.visibility = View.GONE
		cancelButton.visibility = View.GONE

		chronometer.visibility = View.INVISIBLE
		chronometer.stop()

		listener?.onCompleted()
	}

	private fun onStart() {
		dontTrackMoves = false
		isCanceled = false

		audioButton.animate()
			.scaleXBy(1f).scaleYBy(1f)
			.setDuration(200).setInterpolator(OvershootInterpolator())
			.start()

		switchTextInput(false)
		lockerContainer.visibility = View.VISIBLE
		cancelLabel.visibility = View.VISIBLE
		microphoneImage.visibility = View.VISIBLE

		chronometer.base = SystemClock.elapsedRealtime()
		chronometer.start()
		chronometer.visibility = View.VISIBLE

		listener?.onStarted()
	}

	private fun reset() {
		dontTrackMoves = true

		slideDirection = Direction.NONE

		audioButton.animate()
			.scaleX(1f).scaleY(1f)
			.translationX(0f).translationY(0f)
			.setDuration(200).setInterpolator(OvershootInterpolator())
			.start()

		cancelLabel.translationX = 0f
		cancelLabel.visibility = View.GONE

		lockerContainer.visibility = View.GONE
		lockerContainer.translationY = 0f
	}

	private fun switchTextInput(show: Boolean) {
		val visiblitiy = if (show) View.VISIBLE else View.INVISIBLE
		textInput.visibility = visiblitiy
		attachmentButton.visibility = visiblitiy
	}

	fun setListener(listener: Listener? = null) {
		this.listener = listener
	}
}
