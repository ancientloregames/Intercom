package com.ancientlore.intercom.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.ancientlore.intercom.R;
import com.ancientlore.intercom.utils.extensions.BasicExtensionsKt;
import com.bumptech.glide.Glide;

import java.io.File;

public class ChatImageView extends AppCompatImageView
{
	private final ChatImageProgressDrawable progressDrawable;

	private float progressRadius;

	private float progressWidth;

	private float cornerRadius;

	private int backgroundColor;

	private int progressColor;

	public ChatImageView(Context context)
	{
		this(context, null, 0);
	}

	public ChatImageView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ChatImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		setScaleType(ScaleType.CENTER_CROP);

		Resources resources = context.getResources();

		progressRadius = resources.getDimension(R.dimen.chatImageProgressRadius);

		progressWidth = resources.getDimension(R.dimen.chatImageProgressWidth);

		cornerRadius = resources.getDimension(R.dimen.chatImageCornerRadius);

		backgroundColor = ContextCompat.getColor(context, R.color.chatImageBgColor);

		progressColor = ContextCompat.getColor(context, R.color.chatImageProgressColor);

		progressDrawable = new ChatImageProgressDrawable(getContext());
		progressDrawable.setCornerRadius(cornerRadius);
		progressDrawable.setStrokeWidth(progressWidth);
		progressDrawable.setCenterRadius(progressRadius);
		progressDrawable.setBackColor(backgroundColor);
		progressDrawable.setColorSchemeColors(progressColor);
	}

	@Override
	public void setImageDrawable(@Nullable Drawable drawable)
	{
		Drawable d = drawable instanceof ChatImageProgressDrawable
				? drawable
				: RoundedDrawable.fromDrawable(drawable, cornerRadius);

		super.setImageDrawable(d);
	}

	@Override
	public void setImageBitmap(Bitmap bm)
	{
		Drawable d = RoundedDrawable.fromBitmap(bm, cornerRadius);
		super.setImageDrawable(d);
	}

	@Override
	public final void setScaleType(ScaleType scaleType)
	{
		if (scaleType == ScaleType.CENTER_CROP)
			super.setScaleType(scaleType);
		else Log.e(getClass().getSimpleName(), "Only center crop scale type is allowed");
	}

	@Override
	public void setImageURI(@Nullable Uri uri)
	{
		if (uri != null && uri != Uri.EMPTY) {
			if ("file".equals(uri.getScheme())) {
				String lastSegment = uri.getLastPathSegment();
				String filename = lastSegment.substring(lastSegment.lastIndexOf("/") + 1);

				File file = new File(BasicExtensionsKt.getAppCacheDir(getContext()), filename);
				if (file.exists()) {
					Glide.with(getContext())
							.load(file)
							.into(this);
					return;
				}
			}

			Glide.with(getContext())
					.load(uri)
					.placeholder(progressDrawable)
					.into(this);
			progressDrawable.start();

			setVisibility(VISIBLE);
		} else {
			setVisibility(GONE);

			super.setImageURI(null);
		}
	}
}
