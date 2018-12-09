package com.ancientlore.intercom.utils

import androidx.room.TypeConverter
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import com.jsoniter.spi.TypeLiteral

class DataConverters {
	@TypeConverter
	fun deserializeStrList(str: String) = JsonIterator.deserialize(str, object : TypeLiteral<ArrayList<String>>() {})!!

	@TypeConverter
	fun serializeStrList(list: ArrayList<String>) = JsonStream.serialize(list)!!
}