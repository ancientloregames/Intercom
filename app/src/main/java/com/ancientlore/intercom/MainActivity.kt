package com.ancientlore.intercom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ancientlore.intercom.ui.chatlist.ChatListFragment

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)
		if (savedInstanceState == null)
			attachMainUi()
	}

	private fun attachMainUi() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, createMainFragment())
			.commitNow()
	}

	private fun createMainFragment() = ChatListFragment.newInstance()
}
