package com.ancientlore.intercom.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PushMessage implements Parcelable
{
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_CHAT_MESSAGE = 1;

	@IntDef({ TYPE_UNKNOWN, TYPE_CHAT_MESSAGE })
	@Retention(RetentionPolicy.SOURCE)
	@interface Type {}

	public String title = "";
	public String body = "";
	public String chatId = "";

	@Type
	public int type = TYPE_UNKNOWN;

	public PushMessage() { }

	protected PushMessage(@NotNull Parcel in)
	{
		title = in.readString();
		body = in.readString();
		chatId = in.readString();
		type = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(title);
		dest.writeString(body);
		dest.writeString(chatId);
		dest.writeInt(type);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Creator<PushMessage> CREATOR = new Creator<PushMessage>()
	{
		@Override
		public PushMessage createFromParcel(Parcel in)
		{
			return new PushMessage(in);
		}

		@Override
		public PushMessage[] newArray(int size)
		{
			return new PushMessage[size];
		}
	};
}
