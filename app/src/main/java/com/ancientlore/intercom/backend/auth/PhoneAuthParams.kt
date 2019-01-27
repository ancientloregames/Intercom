package com.ancientlore.intercom.backend.auth

import android.os.Parcel
import android.os.Parcelable

data class PhoneAuthParams(val phone: String) : Parcelable {

	constructor(parcel: Parcel) : this(parcel.readString()!!)

	override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeString(phone)

	override fun describeContents() = 0

	companion object CREATOR : Parcelable.Creator<PhoneAuthParams> {
		override fun createFromParcel(parcel: Parcel) = PhoneAuthParams(parcel)

		override fun newArray(size: Int) = arrayOfNulls<PhoneAuthParams?>(size)
	}
}