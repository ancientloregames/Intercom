package com.ancientlore.intercom.utils;

import android.content.res.Resources;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

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

	public static void closeQuietly(Closeable closeable)
	{
		if (closeable == null) return;

		try {
			closeable.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getMimeType(@NotNull String filePath)
	{
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(filePath).toLowerCase());
	}

	public static boolean isImage(@NotNull String filePath)
	{
		String mimeType = getMimeType(filePath);
		return mimeType != null && mimeType.startsWith("image");
	}

	public static boolean isVideo(@NotNull String filePath)
	{
		String mimeType = getMimeType(filePath);
		return mimeType != null && mimeType.startsWith("video");
	}

	@NotNull
	public static String getExtension(String filePath)
	{
		int length = filePath != null ? filePath.lastIndexOf('.') : 0;
		return length > 0 ? filePath.substring(length + 1).toLowerCase() : "";
	}

	public static int toDp(int px)
	{
		return (int) (px * Resources.getSystem().getDisplayMetrics().density);
	}
}
