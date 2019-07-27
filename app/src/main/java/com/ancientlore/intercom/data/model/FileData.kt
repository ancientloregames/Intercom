package com.ancientlore.intercom.data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class FileData(val id: Long = 0,
                    val name: String = "",
                    val uri: Uri = Uri.EMPTY,
                    val size: Long = 0,
                    val extension: String = "") : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readLong(),
		parcel.readString()!!,
		parcel.readParcelable(Uri::class.java.classLoader)!!,
		parcel.readLong(),
		parcel.readString()!!)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(id)
		parcel.writeString(name)
		parcel.writeParcelable(uri, flags)
		parcel.writeLong(size)
		parcel.writeString(extension)
	}

	override fun describeContents() = 0

	fun getInfo() = "${getSizeMb()} MB ${extension.toUpperCase()}"

	private fun getSizeMb() = String.format("%.2f", size / 1048576f)

	companion object CREATOR : Parcelable.Creator<FileData> {
		override fun createFromParcel(parcel: Parcel) = FileData(parcel)

		override fun newArray(size: Int) = arrayOfNulls<FileData?>(size)
	}
}
