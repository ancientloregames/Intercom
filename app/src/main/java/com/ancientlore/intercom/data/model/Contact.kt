package com.ancientlore.intercom.data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.ancientlore.intercom.utils.Identifiable
import com.google.firebase.firestore.Exclude

@Entity(tableName = "contacts",
  indices = [
    Index("userId")
  ])
data class Contact(@field:PrimaryKey var phone: String = "",
                   @field:ColumnInfo var name: String = "",
                   @field:ColumnInfo var chatId: String = "",
                   @field:ColumnInfo var iconUrl: String = "",
                   @field:ColumnInfo @get:Exclude var userId: String = "")
  : Comparable<Contact>, Parcelable, Identifiable<String> {

  companion object CREATOR : Parcelable.Creator<Contact> {

    override fun createFromParcel(parcel: Parcel): Contact {
      return Contact(parcel)
    }

    override fun newArray(size: Int): Array<Contact?> {
      return arrayOfNulls(size)
    }
  }

  @delegate:Exclude @delegate:Ignore @get:Exclude @get:Ignore
  val iconUri: Uri by lazy { Uri.parse(iconUrl) }

  @field:Ignore @get:Exclude
  var checked: Boolean = false

  constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString()
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(phone)
    parcel.writeString(name)
    parcel.writeString(chatId)
    parcel.writeString(iconUrl)
    parcel.writeString(userId)
  }

  override fun describeContents() = 0

  override fun compareTo(other: Contact) = name.compareTo(other.name)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Contact

    return phone == other.phone
        && name == other.name
        && chatId == other.chatId
        && iconUrl == other.iconUrl
        && checked == other.checked
  }

  override fun hashCode(): Int {
    var result = phone.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + chatId.hashCode()
    result = 31 * result + iconUrl.hashCode()
    result = 31 * result + userId.hashCode()
    result = 31 * result + checked.hashCode()
    return result
  }

  @Exclude @Ignore
  override fun getIdentity() = phone

  fun contains(string: CharSequence, ignoreCase: Boolean = true) =
    name.contains(string, ignoreCase) || phone.contains(string, ignoreCase)
}