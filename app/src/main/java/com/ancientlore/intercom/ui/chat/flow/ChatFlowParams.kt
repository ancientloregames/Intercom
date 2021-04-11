package com.ancientlore.intercom.ui.chat.flow

import android.os.Parcel
import android.os.Parcelable

data class ChatFlowParams(val userId: String = "",
                          val chatId: String = "",
                          val title: String = "",
                          val contactId: String = "") : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readString(),
		parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(userId)
		parcel.writeString(chatId)
		parcel.writeString(title)
		parcel.writeString(contactId)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<ChatFlowParams> {
		override fun createFromParcel(parcel: Parcel): ChatFlowParams {
			return ChatFlowParams(parcel)
		}

		override fun newArray(size: Int): Array<ChatFlowParams?> {
			return arrayOfNulls(size)
		}
	}
}