package com.ancientlore.intercom.utils.extensions

import android.os.Parcel
import java.util.*

fun Parcel.writeDate(date: Date) {
	writeLong(date.time)
}

fun Parcel.readDate() : Date {
	return Date(readLong())
}

fun Parcel.writeBoolean(boolean: Boolean) {
	writeByte(if (boolean) 1 else 0)
}

fun Parcel.readBoolean() : Boolean {
	return readByte() != 0.toByte()
}