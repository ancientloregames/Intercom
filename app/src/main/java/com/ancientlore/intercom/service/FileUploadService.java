package com.ancientlore.intercom.service;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ancientlore.intercom.App;
import com.ancientlore.intercom.C;
import com.ancientlore.intercom.MainActivity;
import com.ancientlore.intercom.R;
import com.ancientlore.intercom.backend.ProgressRequestCallback;
import com.ancientlore.intercom.data.model.FileData;
import com.ancientlore.intercom.utils.ImageUtils;
import com.ancientlore.intercom.utils.Utils;
import com.ancientlore.intercom.utils.extensions.BasicExtensionsKt;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileUploadService extends BasicService
{
	protected static class Params
	{
		protected final List<Uri> uriList;
		protected final String path;
		protected final boolean showNotice;
		protected final boolean broadcast;

		protected Params(Intent intent)
		{
			this.uriList = intent.getParcelableArrayListExtra(EXTRA_URI_LIST);

			this.path = intent.getStringExtra(EXTRA_PATH);

			this.showNotice = intent.getBooleanExtra(EXTRA_NOTIFY, false);

			this.broadcast = intent.getBooleanExtra(EXTRA_BROADCAST, false);
		}

		protected boolean isValid()
		{
			return uriList != null && path != null;
		}
	}

	private static final String TAG = FileUploadService.class.getSimpleName();

	public static final String ACTION_UPLOAD = "action_upload";
	public static final String UPLOAD_COMPLETED = "upload_completed";
	public static final String UPLOAD_ERROR = "upload_error";

	public static final String EXTRA_URI_LIST = "extra_files";              // List<Uri>
	public static final String EXTRA_IMAGE = "extra_image";                 // Uri
	public static final String EXTRA_PATH = "extra_path";                   // String
	public static final String EXTRA_NOTIFY = "extra_notify";               // boolean
	public static final String EXTRA_BROADCAST = "extra_notify";            // boolean
	public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";   // Uri

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
		if (ACTION_UPLOAD.equals(intent.getAction()))
		{
			Params params = new Params(intent);
			if (params.isValid())
			{
				for (Uri uri : params.uriList)
				{
					try {
						taskStarted();
						FileData file = compress(uri);
						if (file != null)
							upload(file, params);
					} catch (IOException e) {
						e.printStackTrace();
						taskCompleted();
					}
				}
			}
			else taskCompleted();
		}

		return START_REDELIVER_INTENT;
	}

	private void upload(FileData fileData, Params params) throws IOException
	{
		String rootDir = fileData.getMimeType().startsWith("image") ? "image/" : "file/";
		App.Companion.getBackend().getStorageManager().upload(fileData, rootDir + params.path, new ProgressRequestCallback<Uri>() {
			@Override
			public void onProgress(int progress)
			{
				if (params.showNotice)
					showUploadProgressNotification(progress);
			}
			@Override
			public void onSuccess(Uri result)
			{
				Log.d(TAG, "upload: onSuccess");

				handleUploadFinish(result, fileData.getUri(), params);

				taskCompleted();
			}
			@Override
			public void onFailure(@NotNull Throwable error)
			{
				Utils.logError("FileUploadService.upload.storage.onFailure. uri: " + fileData.getUri(), error);

				handleUploadFinish(null, fileData.getUri(), params);

				taskCompleted();
			}
		});
	}

	@Nullable
	protected FileData compress(Uri imageUri) throws IOException
	{
		FileData fileData = BasicExtensionsKt.getFileData(imageUri, getContentResolver());
		File file = new File(BasicExtensionsKt.getAppCacheDir(this), fileData.getName());

		if (!file.exists() && file.createNewFile())
			ImageUtils.compressImage(getContentResolver(), imageUri, C.MAX_ATTACH_IMG_SIZE_PX, file);

		if (file.exists())
			return fileData;
		else
			Utils.logError("ChatCreationDescFragment.handleActivityResult(): Failed to create file");

		return null;
	}

	protected void handleUploadFinish(@Nullable Uri downloadUrl, Uri fileUri, Params params)
	{
		if (params.broadcast)
			broadcastUploadFinished(downloadUrl, fileUri);
		if (params.showNotice)
			showUploadFinishedNotification(downloadUrl, fileUri);
	}

	protected boolean broadcastUploadFinished(@Nullable Uri downloadUrl, Uri fileUri)
	{
		String action = downloadUrl != null ? UPLOAD_COMPLETED : UPLOAD_ERROR;

		Intent broadcast = new Intent(action)
				.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
				.putExtra(EXTRA_IMAGE, fileUri);
		return LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(broadcast);
	}

	protected void showUploadProgressNotification(int progress)
	{
		showProgressNotification(new NotificationParams(
				android.R.drawable.stat_sys_upload,
				R.string.uploading,
				progress));
	}

	protected void showUploadFinishedNotification(@Nullable Uri downloadUrl, Uri fileUri)
	{
		dismissProgressNotification();

		Intent intent = new Intent(this, MainActivity.class)
				.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
				.putExtra(EXTRA_IMAGE, fileUri)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		boolean success = downloadUrl != null;

		showFinishedNotification(intent, new NotificationParams(
				success ? android.R.drawable.stat_sys_upload_done : android.R.drawable.stat_sys_warning,
				success ? R.string.upload_finished : R.string.upload_failed));
	}
}