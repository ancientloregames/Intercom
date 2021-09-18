package com.ancientlore.intercom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.ancientlore.intercom.R;

public class SimpleCircleImageView extends AppCompatImageView
{
	private final Paint backgroundPaint = new Paint();

	private int backgroundColor = Color.TRANSPARENT;

	public SimpleCircleImageView(Context context) {
		super(context);
	}

	public SimpleCircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleCircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

		try {
			backgroundColor = a.getColor(R.styleable.CircleImageView_backgroundColor, Color.TRANSPARENT);
		} finally {
			a.recycle();
		}

		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setAntiAlias(true);
		backgroundPaint.setColor(backgroundColor);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (backgroundColor != Color.TRANSPARENT)
		{
			float cx = getMeasuredWidth() / 2f;
			float cy = getMeasuredHeight() / 2f;
			canvas.drawCircle(cx, cy, cy, backgroundPaint);
		}

		super.onDraw(canvas);
	}

	public void setBackgroundColor(int color)
	{
		backgroundColor = color;
		invalidate();
	}
}
