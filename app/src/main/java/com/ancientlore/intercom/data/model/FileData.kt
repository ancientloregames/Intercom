package com.ancientlore.intercom.data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class FileData(val id: String,
                    val name: String,
                    val uri: Uri) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readParcelable(Uri::class.java.classLoader))

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(name)
		parcel.writeParcelable(uri, flags)
	}

	override fun describeContents() = 0

	companion object CREATOR : Parcelable.Creator<FileData> {
		override fun createFromParcel(parcel: Parcel) = FileData(parcel)

		override fun newArray(size: Int) = arrayOfNulls<FileData?>(size)
	}
}
