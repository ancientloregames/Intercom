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

	public static void logError(@NotNull Throwable throwable)
	{
		// TODO: use fabric crashlytycs logException later
		throwable.printStackTrace();
	}
}
