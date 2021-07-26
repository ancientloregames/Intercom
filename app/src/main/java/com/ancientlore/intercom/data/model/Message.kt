package com.ancientlore.intercom.data.model

import android.net.Uri
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import androidx.room.*
import com.ancientlore.intercom.utils.Identifiable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.text.DateFormat
import java.text.DateFormat.SHORT
import java.util.*

@Entity(tableName = "messages",
	indices = [
		Index("chatId")
	])
data class Message(@field:ColumnInfo var id: String = "",
                   @field:[ColumnInfo ServerTimestamp] var timestamp: Date? = null,
                   @field:ColumnInfo var senderId: String = "",
                   @field:ColumnInfo var text: String = "",
                   @field:ColumnInfo var info: String = "",
                   @field:ColumnInfo var attachUrl: String = "",
                   @field:[ColumnInfo Type] var type: String = TYPE_TEXT,
                   @field:[ColumnInfo Status] var status: Int = STATUS_WAIT,
                   @field:[Exclude Ignore] var progress: Int = -1,
                   @field:ColumnInfo @get:Exclude var chatId: String = "",
                   @field:[PrimaryKey(autoGenerate = true) Exclude] var localId: Long = 0,// field "id" is unique only per chat
                   @field:[Exclude Ignore] var receiverId: String? = null // FIXME for e2e encryption. Need refactor
)
	: Comparable<Message>, Identifiable<String> {

	companion object {
    const val TYPE_TEXT = "text"
    const val TYPE_IMAGE = "image"
    const val TYPE_FILE = "file"
		const val TYPE_AUDIO = "audio"

	  const val STATUS_WAIT = 0
	  const val STATUS_SENT = 1
	  const val STATUS_RECEIVED = 2

		fun createFromAudio(senderId: String, filePath: String) : Message {
			return Message(senderId = senderId, attachUrl = filePath, type = TYPE_AUDIO)
		}
  }

  constructor(senderId: String, fileData: FileData) : this(
	  "", Date(System.currentTimeMillis()), senderId, fileData.name, fileData.getInfo(), fileData.uri.toString(), TYPE_FILE)

	@StringDef(TYPE_TEXT, TYPE_IMAGE)
  @Retention(AnnotationRetention.SOURCE)
  annotation class Type

	@IntDef(STATUS_WAIT, STATUS_SENT, STATUS_RECEIVED)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Status

  @delegate:[Exclude Ignore] @get:[Exclude Ignore]
  val attachUri: Uri by lazy { if (attachUrl.isNotEmpty()) Uri.parse(attachUrl) else Uri.EMPTY }

  @delegate:[Exclude Ignore] @get:[Exclude Ignore]
  val formatedTime: String by lazy { if (timestamp != null) DateFormat.getTimeInstance(SHORT).format(timestamp) else "" }

  override fun compareTo(other: Message) = timestamp?.compareTo(other.timestamp) ?: 0

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Message

		return timestamp == other.timestamp
				&& status == other.status
				&& senderId == other.senderId
				&& attachUrl == other.attachUrl
				&& progress == other.progress
	}

	override fun hashCode(): Int {
		var result = timestamp.hashCode()
		result = 31 * result + senderId.hashCode()
		result = 31 * result + text.hashCode()
		result = 31 * result + info.hashCode()
		result = 31 * result + attachUrl.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + status
		return result
	}

	fun contains(text: String) = this.text.contains(text, true)

	@Exclude @Ignore
	override fun getIdentity() = id

	fun clone(): Message {
		return Message(id, timestamp, senderId, text, info, attachUrl, type, status, progress, chatId, localId, receiverId)
	}
}