package com.ancientlore.intercom.widget;

import android.view.View;

public class CooldownClickListener implements View.OnClickListener
{
	private final Runnable onClick;
	private final int cooldown;

	public CooldownClickListener(Runnable onClick)
	{
		this(onClick, 200);
	}

	public CooldownClickListener(Runnable onClick, int cooldown)
	{
		this.onClick = onClick;
		this.cooldown = cooldown;
	}

	@Override
	public void onClick(View v)
	{
		v.setEnabled(false);
		onClick.run();
		v.postDelayed(() -> v.setEnabled(true), cooldown);
	}
}