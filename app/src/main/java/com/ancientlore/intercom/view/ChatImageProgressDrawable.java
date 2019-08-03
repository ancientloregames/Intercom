package com.ancientlore.intercom.view;

import android.content.Context;
import android.graphics.*;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class ChatImageProgressDrawable extends CircularProgressDrawable
{
	private final Paint paint = new Paint();

	private float cornerRadius = 0f;

	public ChatImageProgressDrawable(@NonNull Context context)
	{
		super(context);

		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
	}

	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawRoundRect(new RectF(getBounds()), cornerRadius, cornerRadius, paint);

		super.draw(canvas);
	}

	public void setCornerRadius(float cornerRadius)
	{
		this.cornerRadius = cornerRadius;
	}

	public void setBackColor(@ColorInt int color)
	{
		paint.setColor(color);
	}
}
