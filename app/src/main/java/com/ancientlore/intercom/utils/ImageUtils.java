package com.ancientlore.intercom.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import androidx.exifinterface.media.ExifInterface;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static com.ancientlore.intercom.utils.Utils.closeQuietly;

public class ImageUtils
{
	private ImageUtils() { }

	public static void compressImage(@NotNull ContentResolver resolver, @NotNull Uri uri,
	                                 int maxSize, File dest) throws IOException
	{
		int rotation;
		InputStream input = null;
		try {
			input = resolver.openInputStream(uri);
			rotation = new ExifInterface(input).getRotationDegrees();
		} finally {
			closeQuietly(input);
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			input = resolver.openInputStream(uri);
			BitmapFactory.decodeStream(input, null, options);
		} finally {
			closeQuietly(input);
		}

		options.inJustDecodeBounds = false;
		options.inSampleSize = getDecodeScale(options, maxSize, maxSize);
		options.inPurgeable = Build.VERSION.SDK_INT < 21;

		Bitmap bitmap;
		try {
			input = resolver.openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(input, null, options);
		} finally {
			closeQuietly(input);
		}

		bitmap = rotateImage(bitmap, rotation);

		FileOutputStream output = null;
		try {
			output = new FileOutputStream(dest);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
		} finally {
			closeQuietly(output);
			bitmap.recycle();
		}
	}

	@Contract(pure = true)
	private static int getDecodeScale(@NotNull BitmapFactory.Options options, int maxWidth, int maxHeight)
	{
		int scale = Math.max(options.outWidth / maxWidth, options.outHeight / maxHeight);

		return scale > 0 ? scale : 1;
	}

	public static Bitmap rotateImage(@NotNull Bitmap img, int degree)
	{
		Bitmap result;
		if (degree != 0)
		{
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
			result = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
			img.recycle();
		}
		else result = img;

		return result;
	}
}
