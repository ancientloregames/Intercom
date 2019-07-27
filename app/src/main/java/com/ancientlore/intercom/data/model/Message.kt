package com.ancientlore.intercom.data.model

import android.net.Uri
import androidx.annotation.StringDef
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.text.DateFormat.SHORT

data class Message(val timestamp: Long = 0,
                   val senderId: String = "",
                   val text: String = "",
                   val attachUrl: String = "",
                   @Type val type: String = TYPE_TEXT)
	: Comparable<Message> {

  companion object {
    const val TYPE_TEXT = "text"
    const val TYPE_IMAGE = "image"
  }

  @StringDef(TYPE_TEXT, TYPE_IMAGE)
  @Retention(AnnotationRetention.SOURCE)
  annotation class Type

  @delegate:Exclude @get:Exclude
  val attachUri: Uri by lazy { Uri.parse(attachUrl) }

  @delegate:Exclude @get:Exclude
  val formatedTime: String by lazy { DateFormat.getTimeInstance(SHORT).format(timestamp) }

  fun hasTimestamp() = timestamp != 0L

  override fun compareTo(other: Message) = timestamp.compareTo(other.timestamp)
}