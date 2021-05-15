package com.ancientlore.intercom.ui.chat.flow

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class ChatFlowParams(val userId: String = "",
                          val chatId: String = "",
                          val title: String = "",
                          val iconUri: Uri = Uri.EMPTY,
                          val participants: List<String> = emptyList()) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readString(),
		parcel.readParcelable(Uri::class.java.classLoader),
		arrayListOf<String>().apply {
			parcel.readList(this, List::class.java.classLoader)
		}
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(userId)
		parcel.writeString(chatId)
		parcel.writeString(title)
		parcel.writeParcelable(iconUri, 0)
		parcel.writeList(participants)
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