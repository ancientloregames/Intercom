package com.ancientlore.intercom.data.model

import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.util.*

data class Contact(val phone: String = "",
                   val name: String = "",
                   val chatId: String = "",
                   val lastSeenTime: Long = 0)
  : Comparable<Contact> {

  companion object {
    private val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
  }

  @delegate:Exclude
  val lastSeenDate: String by lazy { dateFormat.format(Date(lastSeenTime)) }

  override fun compareTo(other: Contact) = name.compareTo(other.name)
}