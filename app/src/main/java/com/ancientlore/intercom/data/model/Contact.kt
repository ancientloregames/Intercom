package com.ancientlore.intercom.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.util.*

data class Contact(val phone: String = "",
                   val name: String = "",
                   val chatId: String = "",
                   var iconUrl: String = "",
                   val lastSeenTime: Long = 0)
  : Comparable<Contact>, Parcelable {

  companion object CREATOR : Parcelable.Creator<Contact> {

    private val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    override fun createFromParcel(parcel: Parcel): Contact {
      return Contact(parcel)
    }

    override fun newArray(size: Int): Array<Contact?> {
      return arrayOfNulls(size)
    }
  }

  val id: String get() = phone

  @delegate:Exclude @get:Exclude
  val lastSeenDate: String by lazy { dateFormat.format(Date(lastSeenTime)) }

  constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readLong()
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(phone)
    parcel.writeString(name)
    parcel.writeString(chatId)
    parcel.writeString(iconUrl)
    parcel.writeLong(lastSeenTime)
  }

  override fun describeContents() = 0

  override fun compareTo(other: Contact) = name.compareTo(other.name)

  fun contains(string: CharSequence, ignoreCase: Boolean = true) =
    name.contains(string, ignoreCase) || phone.contains(string, ignoreCase)
}