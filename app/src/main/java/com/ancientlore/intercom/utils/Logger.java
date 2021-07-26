package com.ancientlore.intercom.utils;

import android.util.Log;

import com.ancientlore.intercom.C;

public final class Logger
{
	private final String prefix;

	public Logger(String prefix)
	{
		this.prefix = prefix != null ? prefix + ": " : "";
	}

	public Logger(Class clazz)
	{
		this.prefix = clazz != null ? clazz.getName() + ": " : "";
	}

	public void d(String tag, String text)
	{
		Log.d(tag, prefix + text);
	}

	public void d(String text)
	{
		d(C.DEFAULT_LOG_TAG, text);
	}

	public void w(String tag, String text)
	{
		Log.w(tag, prefix + text);
	}

	public void w(String text)
	{
		w(C.DEFAULT_LOG_TAG, text);
	}

	public void i(String tag, String text)
	{
		Log.i(tag, prefix + text);
	}

	public void i(String text)
	{
		i(C.DEFAULT_LOG_TAG, text);
	}

	public void e(String tag, String text)
	{
		Log.e(tag, prefix + text);
	}

	public void e(String text)
	{
		e(C.DEFAULT_LOG_TAG, text);
	}
}
