package com.ancientlore.intercom.view;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.NonNull;
import com.ancientlore.intercom.utils.extensions.BasicExtensionsKt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class RoundedDrawable extends Drawable
{
	private final Matrix shaderMatrix = new Matrix();
	private final RectF bounds = new RectF();
	private final Paint paint = new Paint();

	private final Bitmap bitmap;

	private boolean mRebuildShader = true;

	private float mCornerRadius = 0f;

	public RoundedDrawable(@NotNull Bitmap bitmap)
	{
		this.bitmap = bitmap;

		bounds.set(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
	}

	@Contract("null, _ -> null")
	public static RoundedDrawable fromBitmap(Bitmap bitmap, float cornerRadius)
	{
		return bitmap != null
				? new RoundedDrawable(bitmap).setCornerRadius(cornerRadius)
				: null;
	}

	@Contract("null, _ -> null")
	public static Drawable fromDrawable(Drawable drawable, float cornerRadius)
	{

		if (drawable instanceof RoundedDrawable)
			return ((RoundedDrawable) drawable).setCornerRadius(cornerRadius);
		else if (drawable instanceof LayerDrawable)
		{
			LayerDrawable ld = (LayerDrawable) drawable;
			int num = ld.getNumberOfLayers();

			for (int i = 0; i < num; i++)
			{
				Drawable d = ld.getDrawable(i);
				ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d, cornerRadius));
			}
			return ld;
		}
		else if (drawable != null)
		{
			Bitmap bm = BasicExtensionsKt.toBitmap(drawable);
			if (bm != null)
				return new RoundedDrawable(bm).setCornerRadius(cornerRadius);
		}

		return drawable;
	}

	@Override
	protected void onBoundsChange(@NonNull Rect bounds)
	{
		super.onBoundsChange(bounds);

		this.bounds.set(bounds);
	}

	@Override
	public void draw(@NonNull Canvas canvas)
	{
		if (mRebuildShader)
		{
			BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			bitmapShader.setLocalMatrix(shaderMatrix);
			paint.setShader(bitmapShader);
			mRebuildShader = false;
		}

		if (mCornerRadius > 0)
			canvas.drawRoundRect(bounds, mCornerRadius, mCornerRadius, paint);
		else canvas.drawRect(bounds, paint);
	}

	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public int getAlpha()
	{
		return paint.getAlpha();
	}

	@Override
	public void setAlpha(int alpha)
	{
		paint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public ColorFilter getColorFilter()
	{
		return paint.getColorFilter();
	}

	@Override
	public void setColorFilter(ColorFilter cf)
	{
		paint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public void setDither(boolean dither)
	{
		paint.setDither(dither);
		invalidateSelf();
	}

	@Override
	public void setFilterBitmap(boolean filter)
	{
		paint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public int getIntrinsicWidth()
	{
		return bitmap.getWidth();
	}

	@Override
	public int getIntrinsicHeight()
	{
		return bitmap.getHeight();
	}

	public float getCornerRadius()
	{
		return mCornerRadius;
	}

	public RoundedDrawable setCornerRadius(float radius)
	{
		if (radius >= 0)
			mCornerRadius = radius;
		return this;
	}
}
