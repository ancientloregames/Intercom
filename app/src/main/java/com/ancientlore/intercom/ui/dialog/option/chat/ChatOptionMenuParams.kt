package com.ancientlore.intercom.ui.dialog.option.chat

import android.os.Parcel
import android.os.Parcelable

data class ChatOptionMenuParams(val pin: Boolean) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readByte() != 0.toByte()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeByte(if (pin) 1 else 0)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<ChatOptionMenuParams> {
		override fun createFromParcel(parcel: Parcel): ChatOptionMenuParams {
			return ChatOptionMenuParams(parcel)
		}

		override fun newArray(size: Int): Array<ChatOptionMenuParams?> {
			return arrayOfNulls(size)
		}
	}
}