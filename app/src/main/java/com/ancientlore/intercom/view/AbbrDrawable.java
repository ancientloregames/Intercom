package com.ancientlore.intercom.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class AbbrDrawable extends Drawable
{
	private final Paint textPaint = new Paint();

	private final String text;
	private final int fontSize;

	public AbbrDrawable(String text, int fontSize)
	{
		this.text = text;
		this.fontSize = fontSize;

		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.WHITE);
		textPaint.setFakeBoldText(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextAlign(Paint.Align.CENTER);

		Rect rect = new Rect();
		textPaint.getTextBounds(text, 0 ,text.length(), rect);
		setBounds(rect);
	}

	@Override
	public void draw(Canvas canvas)
	{
		Rect r = getBounds();

		int count = canvas.save();
		canvas.translate(r.left, r.top);

		// draw text
		int width = r.width();
		int height = r.height();
		int fontSize = this.fontSize < 0 ? (Math.min(width, height) / 2) : this.fontSize;
		textPaint.setTextSize(fontSize);
		canvas.drawText(text, width / 2, height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);

		canvas.restoreToCount(count);

	}

	@Override
	public void setAlpha(int alpha)
	{
		textPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf)
	{
		textPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSLUCENT;
	}
}
