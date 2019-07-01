package com.ancientlore.intercom.data.model

import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.text.DateFormat.SHORT

data class Message(val timestamp: Long = 0,
                   val senderId: String = "",
                   val text: String = "")
	: Comparable<Message> {

  @delegate:Exclude
  val formatedTime: String by lazy { DateFormat.getTimeInstance(SHORT).format(timestamp) }

  fun hasTimestamp() = timestamp != 0L

  override fun compareTo(other: Message) = timestamp.compareTo(other.timestamp)
}