package com.ancientlore.intercom.utils;

import android.net.Uri;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public final class Utils
{
	private Utils() {}

	@NotNull
	public static Uri parseUri(@Nullable String uriStr)
	{
		return uriStr != null ? Uri.parse(uriStr) : Uri.EMPTY;
	}
}
