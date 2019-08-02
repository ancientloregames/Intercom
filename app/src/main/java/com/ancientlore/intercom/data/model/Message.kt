package com.ancientlore.intercom.data.model

import android.net.Uri
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.text.DateFormat.SHORT

data class Message(val id: String = "",
                   val timestamp: Long = System.currentTimeMillis(),// TODO get server timestamp on app start?
                   val senderId: String = "",
                   val text: String = "",
                   val info: String = "",
                   val attachUrl: String = "",
                   @Type val type: String = TYPE_TEXT,
                   @Status val status: Int = STATUS_WAIT,
                   @get:Exclude var progress: Int = -1)
	: Comparable<Message> {

	companion object {
    const val TYPE_TEXT = "text"
    const val TYPE_IMAGE = "image"
    const val TYPE_FILE = "file"

	  const val STATUS_WAIT = 0
	  const val STATUS_SENT = 1
	  const val STATUS_RECEIVED = 2
  }

  constructor(senderId: String, fileData: FileData) : this(
	  "", System.currentTimeMillis(), senderId, fileData.name, fileData.getInfo(), fileData.uri.toString(), TYPE_FILE)

	@StringDef(TYPE_TEXT, TYPE_IMAGE)
  @Retention(AnnotationRetention.SOURCE)
  annotation class Type

	@IntDef(STATUS_WAIT, STATUS_SENT, STATUS_RECEIVED)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Status

  @delegate:Exclude @get:Exclude
  val attachUri: Uri by lazy { Uri.parse(attachUrl) }

  @delegate:Exclude @get:Exclude
  val formatedTime: String by lazy { DateFormat.getTimeInstance(SHORT).format(timestamp) }

  override fun compareTo(other: Message) = timestamp.compareTo(other.timestamp)

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
}