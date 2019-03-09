package com.ancientlore.intercom.data.model

import java.text.DateFormat
import java.text.DateFormat.SHORT

data class Message(val timestamp: Long = 0,
                   val senderId: String = "",
                   val text: String = "") {

  val formatedTime: String by lazy { DateFormat.getTimeInstance(SHORT).format(timestamp) }

  fun hasTimestamp() = timestamp != 0L
}