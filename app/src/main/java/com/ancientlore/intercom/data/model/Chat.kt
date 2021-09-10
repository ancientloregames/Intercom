package com.ancientlore.intercom.data.model

import android.net.Uri
import androidx.annotation.IntDef
import androidx.room.*
import com.ancientlore.intercom.utils.Identifiable
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.util.*

@Entity(tableName = "chats",
	indices = [
		Index("userId")
	])
data class Chat(@field:PrimaryKey @DocumentId var id: String = "",
                @field:ColumnInfo var name: String = "",
                @field:ColumnInfo var iconUrl: String = "",
                @field:ColumnInfo var initiatorId: String = "",
                @field:ColumnInfo var participants: List<String> = emptyList(),
                @field:ColumnInfo var lastMsgSenderId: String = "",
                @field:ColumnInfo var lastMsgTime: Date? = null,
                @field:ColumnInfo var lastMsgText: String = "",
                @field:[ColumnInfo Type] var type: Int = TYPE_PRIVATE,
                @field:ColumnInfo var pin: Boolean? = null,
                @field:ColumnInfo var mute: Boolean? = null,
                @field:ColumnInfo @get:Exclude var userId: String = "")
	: Comparable<Chat>, Identifiable<String> {

	companion object {
		const val TYPE_PRIVATE = 0
		const val TYPE_GROUP = 1
	}

	@IntDef(TYPE_PRIVATE, TYPE_GROUP)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Type

	@delegate:Exclude @delegate:Ignore @get:Exclude @get:Ignore
	val iconUri: Uri by lazy { Uri.parse(iconUrl) }

	@delegate:Exclude @delegate:Ignore @get:Exclude @get:Ignore
	val lastMsgDate: String by lazy { Utils.toHumanReadableTime(lastMsgTime).toString() }

	// TODO maybe shouldn't exclude from room
	@field:Exclude @field:Ignore @get:Exclude @get:Ignore
	var localName: String? = null

	// TODO maybe shouldn't exclude from room
	@field:Exclude @field:Ignore @get:Exclude @get:Ignore
	var lastMsgSenderLocalName: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Chat

		return lastMsgTime == other.lastMsgTime
				&& id == other.id
				&& name == other.name
				&& iconUrl == other.iconUrl
				&& initiatorId == other.initiatorId
				&& participants == other.participants
				&& lastMsgSenderId == other.lastMsgSenderId
				&& lastMsgText == other.lastMsgText
				&& type == other.type
				&& pin == other.pin
				&& mute == other.mute
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + iconUrl.hashCode()
		result = 31 * result + initiatorId.hashCode()
		result = 31 * result + participants.hashCode()
		result = 31 * result + lastMsgSenderId.hashCode()
		result = 31 * result + lastMsgTime.hashCode()
		result = 31 * result + lastMsgText.hashCode()
		result = 31 * result + type.hashCode()
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

	@[Exclude Ignore]
	override fun getIdentity() = id
}
