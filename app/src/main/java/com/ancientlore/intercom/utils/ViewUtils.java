package com.ancientlore.intercom.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.core.view.TintableBackgroundView;
import androidx.core.widget.TintableCompoundButton;

public final class ViewUtils
{
	private ViewUtils() {}

	public static void setImageTint(ImageView view, @ColorInt int color)
	{
		if (color != Color.TRANSPARENT)
			setImageTint(view, ColorStateList.valueOf(color));
	}

	public static void setImageTint(ImageView view, ColorStateList colors)
	{
		if (view != null)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				view.setImageTintList(colors);
			else
			{
				Drawable tintedDrawable = ImageUtils.setTint(view.getDrawable(), colors.getDefaultColor());
				view.setImageDrawable(tintedDrawable);
			}
		}
	}

	public static void setBackgroundTint(View view, @ColorInt int color)
	{
		if (color != Color.TRANSPARENT)
			setBackgroundTint(view, ColorStateList.valueOf(color));
	}

	public static void setBackgroundTint(View view, ColorStateList colors)
	{
		if (view != null)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				view.setBackgroundTintList(colors);
			else  if (view instanceof TintableBackgroundView)
				((TintableBackgroundView) view).setSupportBackgroundTintList(colors);
			else
				view.getBackground().setColorFilter(colors.getDefaultColor(), PorterDuff.Mode.MULTIPLY);
		}
	}

	public static void setButtonTint(View view, @ColorInt int color)
	{
		if (color != Color.TRANSPARENT)
			setButtonTint(view, ColorStateList.valueOf(color));
	}

	public static void setButtonTint(View view, ColorStateList colors)
	{
		if (view != null)
		{
			if (view instanceof CompoundButton && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				((CompoundButton) view).setButtonTintList(colors);
			else if (view instanceof TintableCompoundButton)
				((TintableCompoundButton) view).setSupportButtonTintList(colors);
			else
				setBackgroundTint(view, colors.getDefaultColor());
		}
	}
}
