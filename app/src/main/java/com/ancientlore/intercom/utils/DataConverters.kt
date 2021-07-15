package com.ancientlore.intercom.utils

import androidx.room.TypeConverter
import com.jsoniter.JsonIterator
import com.jsoniter.spi.TypeLiteral
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.*

class DataConverters {

	@TypeConverter
	fun toList(str: String): List<String> {
		return Json.decodeFromString(ListSerializer(String.serializer()), str)
//		return JsonIterator.deserialize(str, object : TypeLiteral<ArrayList<String>>() {})!!
	}

	@TypeConverter
	fun fromList(list: List<String>): String {
		return Json.encodeToString(ListSerializer(String.serializer()), list)
//		return JsonUtils.serialize(list)
	}

	@TypeConverter
	fun toDate(time: Long?): Date? {
		return time?.let { Date(it) }
	}

	@TypeConverter
	fun fromDate(date: Date?): Long? {
		return date?.time
	}
}