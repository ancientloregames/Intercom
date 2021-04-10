package com.ancientlore.intercom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.ancientlore.intercom.R;


public class ColoredProgressBar extends ProgressBar
{
	private int color = Color.WHITE;

	public ColoredProgressBar(Context context)
	{
		super(context);
	}

	public ColoredProgressBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray typedArray = context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.ColoredProgressBar, 0, 0);

		try {
			this.setColor(typedArray.getInteger(R.styleable.ColoredProgressBar_color, color));
		}
		finally {
			typedArray.recycle();
		}
	}

	public ColoredProgressBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void setColor(int color)
	{
		this.color = color;
		this.getIndeterminateDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
	}

	public int getColor()
	{
		return color;
	}
}
