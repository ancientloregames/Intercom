package com.ancientlore.intercom.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.ancientlore.intercom.C;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

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

	public static void logError(@Nullable Throwable throwable, @Nullable String tag, @Nullable String text)
	{
		// TODO: use fabric crashlytycs logException later
		String logTag = tag != null ? tag : C.DEFAULT_LOG_TAG;
		String logText = text != null ? text : "";
		if (throwable != null)
			Log.e(logTag, logText, throwable);
		else
			Log.e(logTag, logText);
	}

	public static void logError(Throwable throwable, @Nullable String tag)
	{
		logError(throwable, tag, null);
	}

	public static void logError(Throwable throwable)
	{
		logError(throwable, null);
	}

	public static void logError(String text)
	{
		if (text != null)
			logError(new RuntimeException(text));
	}

	public static void logError(String text, Throwable throwable)
	{
		if (text != null)
			logError(new RuntimeException(text, throwable));
		else
			logError(throwable);
	}

	public static void logError(String tag, String text)
	{
		if (text != null)
			logError(new RuntimeException(text), tag, null);
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

	public static String toHumanReadablePhone(String phoneNumber, @Nullable String region)
	{
		if (phoneNumber == null)
			return null;

		try {
			PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber pn = pnu.parse(phoneNumber, region != null ? region : "ZZ");
			return pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
		} catch (NumberParseException e) {
			logError(e);
		}
		return phoneNumber;
	}

	public static CharSequence toHumanReadableTime(Date date)
	{
		return date != null ? toHumanReadableTime(date.getTime()) : "";
	}

	public static CharSequence toHumanReadableTime(long time)
	{
		return DateUtils.getRelativeTimeSpanString(time,
				System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
	}

	public static boolean showKeyboard(View view)
	{
		return showKeyboard(view, InputMethodManager.SHOW_IMPLICIT);
	}

	public static boolean showKeyboard(View view, int flags)
	{
		if (view != null)
		{
			Context context = view.getContext();
			if (context != null)
			{
				if (Looper.myLooper() == Looper.getMainLooper())
				{
					return view.requestFocus()
							&& ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
							.showSoftInput(view, flags);
				}
				else
				{
					return view.post(() ->
					{
						view.requestFocus();
						((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
								.showSoftInput(view, flags);
					});
				}
			}
		}
		return false;
	}

	public static boolean hideKeyboard(Activity activity)
	{
		return hideKeyboard(activity, 0);
	}

	public static boolean hideKeyboard(Activity activity, int flags)
	{
		View view = activity.getCurrentFocus();
		return view != null && hideKeyboard(view, flags);
	}

	public static boolean hideKeyboard(View view)
	{
		return hideKeyboard(view, 0);
	}

	public static boolean hideKeyboard(View view, int flags)
	{
		if (view != null)
		{
			Context context = view.getContext();
			if (context != null)
			{
				if (Looper.myLooper() == Looper.getMainLooper())
				{
					((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(view.getWindowToken(), flags);
				}
				else
				{
					return view.post(() ->
							((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(view.getWindowToken(), flags));
				}
			}
		}
		return false;
	}
}
