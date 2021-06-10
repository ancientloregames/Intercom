package com.ancientlore.intercom.data.model

import android.net.Uri
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.util.*

data class User(val name: String = "",
                val phone: String = "",
                val email: String = "",
                val iconUrl: String = "",
                val status: String = "",
                val lastSeenTime: Date = Date(0),
                val online: Boolean = false,
                @get:Exclude val dummy: Boolean = false) {

  companion object {
    private val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
  }

  @get:Exclude
  val id get() = phone

  @delegate:Exclude @get:Exclude
  val iconUri: Uri by lazy { Uri.parse(iconUrl) }

  @delegate:Exclude @get:Exclude
  val lastSeenDate: String by lazy { dateFormat.format(lastSeenTime) }
}