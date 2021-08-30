package com.ancientlore.intercom.ui.call

import android.os.Parcel
import android.os.Parcelable

data class CallAnswerParams(override val targetId: String,
                            val sdp: String,
                            override val name: String? = null,
                            override val iconUrl: String? = null)
	: CallViewModel.Params(targetId, name, iconUrl), Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readString(),
		parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(targetId)
		parcel.writeString(sdp)
		parcel.writeString(name)
		parcel.writeString(iconUrl)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<CallAnswerParams> {
		override fun createFromParcel(parcel: Parcel): CallAnswerParams {
			return CallAnswerParams(parcel)
		}

		override fun newArray(size: Int): Array<CallAnswerParams?> {
			return arrayOfNulls(size)
		}
	}
}