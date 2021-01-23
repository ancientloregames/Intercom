package com.ancientlore.intercom.utils;

import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import kotlin.text.Regex;

public final class Utils
{
	private Utils() {}

	private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

	public static void runOnUiThread(Runnable runnable)
	{
		if (Looper.myLooper() == Looper.getMainLooper())
			runnable.run();
		else
			UI_HANDLER.post(runnable);
	}

	public static void runOnUiThread(Runnable runnable, long delay)
	{
		if (delay > 0)
			UI_HANDLER.postDelayed(runnable, delay);
		else
			runOnUiThread(runnable);
	}

	public static void cancelUiTask(Runnable runnable)
	{
		UI_HANDLER.removeCallbacks(runnable);
	}

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

	public static boolean isExternalUrl(String url)
	{
		return url != null && !url.isEmpty() && (url.startsWith("https://") || url.startsWith("http://") || url.startsWith("ftp://"));
	}

	@NotNull
	public static String getExtension(String filePath)
	{
		int length = filePath != null ? filePath.lastIndexOf('.') : 0;
		return length > 0 ? filePath.substring(length + 1).toLowerCase() : "";
	}

	@Contract("null -> null")
	public static String getFileName(String filePath)
	{
		if (filePath == null)
			return null;

		String decodedPath = filePath.replaceAll("%2F", "/");

		int length = decodedPath.length();

		int startIndex = decodedPath.lastIndexOf('/') + 1;

		int endIndex = decodedPath.lastIndexOf('?');
		if (endIndex == -1)
			endIndex = length;

		return decodedPath.substring(startIndex, endIndex);
	}

	public static long getDuration(File mediaFile)
	{
		long duration = 0;
		if (mediaFile != null && mediaFile.length() > 0)
		{
			try (FileInputStream is = new FileInputStream(mediaFile)) {
				MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
				mediaMetadataRetriever.setDataSource(is.getFD());
				String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				duration = Long.parseLong(durationStr);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return duration;
	}

	public static String getFormatedDuration(long millis)
	{
		int duration = (int) (millis / 1000);
		int sec = duration % 60;
		int min = duration / 60;
		return new StringBuilder()
				.append(min < 10 ? "0" + min : min + "")
				.append(":")
				.append(sec < 10 ? "0" + sec : sec + "").toString();
	}

	public static int toDp(int px)
	{
		return (int) (px * Resources.getSystem().getDisplayMetrics().density);
	}

	public static String formatPhoneNumber(String phoneNumber)
	{
		return phoneNumber != null
				? new Regex(" |-").replace(phoneNumber, "")
				: null;
	}
}
