package com.ancientlore.intercom.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.TintableBackgroundView;
import com.ancientlore.intercom.R;
import org.jetbrains.annotations.NotNull;

public class DrawableCompatTextView extends AppCompatTextView
{
	public DrawableCompatTextView(Context context)
	{
		super(context);
	}

	public DrawableCompatTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initAttrs(context, attrs);
	}

	public DrawableCompatTextView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		initAttrs(context, attrs);
	}

	void initAttrs(Context context, AttributeSet attrs)
	{
		if (attrs != null)
		{
			TypedArray attributeArray = context.obtainStyledAttributes(
					attrs,
					R.styleable.DrawableCompatTextView);

			obtainBackgroundTint(attributeArray);

			Drawable drawableStart = null;
			Drawable drawableEnd = null;
			Drawable drawableBottom = null;
			Drawable drawableTop = null;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				drawableStart = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableStart);
				drawableEnd = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableEnd);
				drawableBottom = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableBottom);
				drawableTop = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableTop);
			}
			else
			{
				 int drawableStartId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableStart, -1);
				 int drawableEndId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableEnd, -1);
				 int drawableBottomId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableBottom, -1);
				 int drawableTopId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableTop, -1);

				if (drawableStartId != -1)
					drawableStart = AppCompatResources.getDrawable(context, drawableStartId);
				if (drawableEndId != -1)
					drawableEnd = AppCompatResources.getDrawable(context, drawableEndId);
				if (drawableBottomId != -1)
					drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
				if (drawableTopId != -1)
					drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
			}

			setCompoundDrawablesCompat(drawableStart, drawableTop, drawableEnd, drawableBottom);

			attributeArray.recycle();
		}
	}

	private void obtainBackgroundTint(@NotNull TypedArray attributes)
	{
		int color = attributes.getColor(R.styleable.DrawableCompatTextView_backgroundTint, -1);
		if (color != -1) setBackgroundTint(color);
	}

	public void setDrawableStart(Drawable drawable)
	{
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawable, drawables[1], drawables[2], drawables[3]);
	}

	public void setDrawableTop(Drawable drawable)
	{
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawable, drawables[2], drawables[3]);
	}

	public void setDrawableEnd(Drawable drawable)
	{
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawables[1], drawable, drawables[3]);
	}

	public void setDrawableBottom(Drawable drawable)
	{
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawables[1], drawables[2], drawable);
	}

	public void setDrawableStart(@DrawableRes int resId)
	{
		Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawable, drawables[1], drawables[2], drawables[3]);
	}

	public void setDrawableTop(@DrawableRes int resId)
	{
		Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawable, drawables[2], drawables[3]);
	}

	public void setDrawableEnd(@DrawableRes int resId)
	{
		Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawables[1], drawable, drawables[3]);
	}

	public void setDrawableBottom(@DrawableRes int resId)
	{
		Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
		Drawable[] drawables = getCompoundDrawables();
		setCompoundDrawablesCompat(drawables[0], drawables[1], drawables[2], drawable);
	}

	public void setCompoundDrawablesCompat(@Nullable Drawable drawableStart, @Nullable Drawable drawableTop,
	                                       @Nullable Drawable drawableEnd, @Nullable Drawable drawableBottom)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom);
		else setCompoundDrawablesWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom);
	}

	public void setBackgroundTint(@ColorInt int color)
	{
		((TintableBackgroundView) this).setSupportBackgroundTintList(ColorStateList.valueOf(color));
	}
}