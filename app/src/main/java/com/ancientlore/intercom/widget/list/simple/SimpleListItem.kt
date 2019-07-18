package com.ancientlore.intercom.widget.list.simple

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes

data class SimpleListItem(@IdRes val id: Int,
                          @StringRes val title: Int,
                          @DrawableRes val icon: Int = -1) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readInt(),
		parcel.readInt())

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeInt(title)
		parcel.writeInt(icon)
	}

	override fun describeContents() = 0

	companion object CREATOR : Parcelable.Creator<SimpleListItem> {
		override fun createFromParcel(parcel: Parcel) = SimpleListItem(parcel)

		override fun newArray(size: Int) = arrayOfNulls<SimpleListItem?>(size)
	}
}