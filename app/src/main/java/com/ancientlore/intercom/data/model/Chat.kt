package com.ancientlore.intercom.data.model

import android.net.Uri
import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.*


data class Chat(val id: String = "",
                val name: String = "",
                val iconUrl: String = "",
                val initiatorId: String = "",
                val participants: List<String> = emptyList(),
                val lastMsgSenderId: String = "",
                val lastMsgTime: Date = Date(0),
                val lastMsgText: String = "",
                val type: Int = TYPE_PRIVATE,
                val pin: Boolean? = null,
                val mute: Boolean? = null)
	: Comparable<Chat> {

	companion object {
		const val TYPE_PRIVATE = 0
		const val TYPE_GROUP = 1

		private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
	}

	@delegate:Exclude @get:Exclude
	val iconUri: Uri by lazy { Uri.parse(iconUrl) }

	@delegate:Exclude @get:Exclude
	val lastMsgDate: String by lazy { if (lastMsgTime != null) dateFormat.format(lastMsgTime) else "" }

	@set:Exclude @get:Exclude
	var localName: String? = null

	@set:Exclude @get:Exclude
	var lastMsgSenderLocalName: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Chat

		return lastMsgTime == other.lastMsgTime
				&& id == other.id
				&& name == other.name
				&& initiatorId == other.initiatorId
				&& participants == other.participants
				&& lastMsgText == other.lastMsgText
				&& pin == other.pin
				&& mute == other.mute
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + initiatorId.hashCode()
		result = 31 * result + participants.hashCode()
		result = 31 * result + lastMsgTime.hashCode()
		result = 31 * result + lastMsgText.hashCode()
		pin?.let { result = 31 * result + pin.hashCode() }
		mute?.let { result = 31 * result + mute.hashCode() }
		return result
	}

	override fun compareTo(other: Chat) : Int {
		var weight = 0 // lastMsgTime.compareTo(other.lastMsgTime) FIXME for some reason Firestore returns null in non-nullable value
		if (pin == true) weight += 2
		if (other.pin == true) weight -= 2
		return weight

	}

	fun contains(text: String): Boolean {
		return name.contains(text, true)
				|| lastMsgText.contains(text, true)
				|| localName?.contains(text, true) == true
	}
}
