package com.ancientlore.intercom.crypto;

import android.util.Base64;

import java.nio.charset.Charset;

public final class CryptoUtils
{
	private final static Charset DEF_CHARSET = Charset.forName("UTF-8");

	private CryptoUtils() {}

	public static byte[] decode(byte[] bytes)
	{
		return Base64.decode(bytes, Base64.DEFAULT);
	}

	public static byte[] decode(String str)
	{
		return decode(str.getBytes(DEF_CHARSET));
	}

	public static String decodeToString(byte[] bytes)
	{
		return new String(decode(bytes), DEF_CHARSET);
	}

	public static String decodeToString(String str)
	{
		return decodeToString(str.getBytes(DEF_CHARSET));
	}

	public static byte[] encode(byte[] bytes)
	{
		return Base64.encode(bytes, Base64.DEFAULT);
	}

	public static byte[] encode(String str)
	{
		return encode(str.getBytes(DEF_CHARSET));
	}

	public static String encodeToString(byte[] bytes)
	{
		return new String(encode(bytes), DEF_CHARSET);
	}

	public static String encodeToString(String str)
	{
		return encodeToString(str.getBytes(DEF_CHARSET));
	}

	public static byte[] toBytes(String str)
	{
		return str.getBytes(DEF_CHARSET);
	}

	public static String toString(byte[] bytes)
	{
		return new String(bytes, DEF_CHARSET);
	}
}
