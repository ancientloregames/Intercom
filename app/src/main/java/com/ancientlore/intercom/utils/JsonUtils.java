package com.ancientlore.intercom.utils;

import android.util.Log;
import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonUtils
{
	private JsonUtils() { }

	@NotNull
	public static String serialize(@NotNull Object jsonItem)
	{
		return JsonStream.serialize(jsonItem);
	}

	public static <T> T deserialize(byte[] input, Class<T> clazz)
	{
		return JsonIterator.deserialize(input, clazz);
	}

	public static <T> T deserialize(@NotNull String jsonItem, Class<T> clazz)
	{
		return deserialize(jsonItem.getBytes(), clazz);
	}

	public static <T> T deserialize(@NotNull JSONObject jsonItem, Class<T> clazz)
	{
		return deserialize(jsonItem.toString(), clazz);
	}

	public static <T> T deserialize(@NotNull Map map, Class<T> clazz)
	{
		return deserialize(new JSONObject(map), clazz);
	}

	public static <T> List<T> deserialize(@NotNull List<JSONObject> jsonItems, Class<T> clazz)
	{
		List<T> items = new ArrayList<>(jsonItems.size());
		for (JSONObject jsonItem: jsonItems)
		{
			items.add(deserialize(jsonItem.toString(), clazz));
		}
		return items;
	}

	public static <T> List<T> deserialize(@NotNull JSONArray jsonItems, Class<T> clazz)
	{
		List<T> items = new ArrayList<>(jsonItems.length());
		for (int i = 0; i < jsonItems.length(); i++)
		{
			JSONObject jsonItem = jsonItems.optJSONObject(i);
			items.add(deserialize(jsonItem.toString(), clazz));
		}
		return items;
	}

	public static <T> void deserialize(@NotNull List<JSONObject> jsonItems, @NotNull T[] dest, @NotNull Class<T> clazz)
	{
		if (dest.length == jsonItems.size())
			for (int i = 0; i < dest.length ; i++)
				dest[i] = deserialize(jsonItems.get(i).toString(), clazz);
		else Log.e("JsonUtils", "Error! The destination array and source list must have the same size");
	}
}
