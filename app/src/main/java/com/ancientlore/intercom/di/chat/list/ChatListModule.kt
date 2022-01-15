package com.ancientlore.intercom.di.chat.list

import android.view.LayoutInflater
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ChatListModule {

	@ChatListScreenScope
	@Provides
	@Named("ChatListInflater")
	fun layoutInflater(fragment: ChatListFragment): LayoutInflater {
		return fragment.layoutInflater
	}
}