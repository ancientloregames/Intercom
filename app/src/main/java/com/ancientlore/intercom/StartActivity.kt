package com.ancientlore.intercom

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StartActivity: AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		openMainActivity()
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent == null) {
			openMainActivity(intent)
		}
	}

	private fun openMainActivity(outerIntent: Intent? = null) {

		val intent = outerIntent?.let {
			Intent(it).apply {
				component = ComponentName(this@StartActivity, MainActivity::class.java)
			}
		} ?: Intent(this@StartActivity, MainActivity::class.java)

		intent.apply {
			overridePendingTransition(0, 0)
			flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
			startActivity(this)
		}

		finish()
	}
}