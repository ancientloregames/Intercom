package com.ancientlore.intercom.data.model

import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.*


data class Chat(val chatId: String = "",
                val name: String = "",
                val lastMsgTime: Long = 0,
                val lastMsgText: String = "")
	: Comparable<Chat> {

	companion object {
		private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
	}

	@delegate:Exclude
	val lastMsgDate: String by lazy { dateFormat.format(Date(lastMsgTime)) }

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Chat

		return lastMsgTime == other.lastMsgTime
				&& chatId == other.chatId
				&& name == other.name
				&& lastMsgText == other.lastMsgText
	}

	override fun hashCode(): Int {
		var result = chatId.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + lastMsgTime.hashCode()
		result = 31 * result + lastMsgText.hashCode()
		return result
	}

	override fun compareTo(other: Chat) = lastMsgTime.compareTo(other.lastMsgTime)

	fun assignId(id: String) = Chat(id, name, lastMsgTime, lastMsgText)
}
