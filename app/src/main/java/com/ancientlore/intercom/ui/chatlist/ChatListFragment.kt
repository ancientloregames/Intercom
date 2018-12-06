package com.ancientlore.intercom.ui.chatlist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ancientlore.intercom.R

class ChatListFragment : Fragment() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	private lateinit var viewModel: ChatsListViewModel

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.chats_list_fragment, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		viewModel = ViewModelProviders.of(this).get(ChatsListViewModel::class.java)
		// TODO: Use the ViewModel
	}
}
