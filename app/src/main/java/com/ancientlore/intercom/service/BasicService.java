package com.ancientlore.intercom.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ancientlore.intercom.R;

public abstract class BasicService extends Service
{
	protected static final class NotificationParams
	{
		private final int smallIcon;
		private final int contentText;
		private final int progress;

		public NotificationParams(int smallIcon, int text, int progress)
		{
			this.smallIcon = smallIcon;
			this.contentText = text;
			this.progress = progress;
		}

		public NotificationParams(int smallIcon, int text)
		{
			this.smallIcon = smallIcon;
			this.contentText = text;
			this.progress = 0;
		}
	}

	private static final String TAG = BasicService.class.getSimpleName();

	public static final int CODE_FINISHED = 901;

	static final int PROGRESS_NOTIFICATION_ID = 0;
	static final int FINISHED_NOTIFICATION_ID = 1;

	private int mumTasks = 0;

	private String notificationChannelId;

	@Override
	public void onCreate()
	{
		super.onCreate();
		notificationChannelId = getResources().getString(R.string.basic_notification_channel_id);
	}

	public void taskStarted()
	{
		changeNumberOfTasks(1);
	}

	public void taskCompleted()
	{
		changeNumberOfTasks(-1);
	}

	private synchronized void changeNumberOfTasks(int delta)
	{
		Log.d(TAG, "changeNumberOfTasks:" + mumTasks + ":" + delta);
		mumTasks += delta;

		if (mumTasks <= 0)
			stopSelf();
	}

	protected void dismissProgressNotification()
	{
		NotificationManager manager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.cancel(PROGRESS_NOTIFICATION_ID);
	}

	protected void showProgressNotification(@NonNull NotificationParams params)
	{
		Log.d(TAG, "showProgressNotification:" + params.progress);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId)
				.setSmallIcon(params.smallIcon)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(params.contentText))
				.setProgress(100, params.progress, false)
				.setOngoing(true)
				.setAutoCancel(false);

		NotificationManager manager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.notify(PROGRESS_NOTIFICATION_ID, builder.build());
	}

	protected void showFinishedNotification(@NonNull Intent intent, @NonNull NotificationParams params)
	{
		Log.d(TAG, "showFinishedNotification: " + params.contentText);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, CODE_FINISHED, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId)
				.setSmallIcon(params.smallIcon)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(params.contentText))
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);

		NotificationManager manager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.notify(FINISHED_NOTIFICATION_ID, builder.build());
	}
}