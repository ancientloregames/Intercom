package com.ancientlore.intercom.ui.contact.detail

import android.os.Parcel
import android.os.Parcelable

data class ContactDetailParams(val id: String,
                               val name: String,
                               val iconUrl: String,
                               val openedFromChat: Boolean,
                               val chatId: String? = null)
	: Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readString(),
		parcel.readByte() != 0.toByte(),
		parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(name)
		parcel.writeString(iconUrl)
		parcel.writeByte(if (openedFromChat) 1 else 0)
		parcel.writeString(chatId)
	}

	override fun describeContents() = 0

	companion object CREATOR : Parcelable.Creator<ContactDetailParams> {
		override fun createFromParcel(parcel: Parcel) = ContactDetailParams(parcel)

		override fun newArray(size: Int) = arrayOfNulls<ContactDetailParams?>(size)
	}
}