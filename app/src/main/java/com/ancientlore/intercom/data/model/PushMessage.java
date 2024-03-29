package com.ancientlore.intercom.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.StringDef;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PushMessage implements Parcelable
{
	public static final String TYPE_UNKNOWN = "";
	public static final String TYPE_CHAT_MESSAGE = "chat-message";

	@StringDef({ TYPE_UNKNOWN, TYPE_CHAT_MESSAGE })
	@Retention(RetentionPolicy.SOURCE)
	@interface Type {}

	public String id = "";
	public String title = "";
	public String body = "";
	public String chatId = "";
	public int chaType = 0;
	public String iconUrl = "";

	@Type
	public String type = TYPE_UNKNOWN;

	public PushMessage() { }

	protected PushMessage(@NotNull Parcel in)
	{
		id = in.readString();
		title = in.readString();
		body = in.readString();
		chatId = in.readString();
		chaType = in.readInt();
		type = in.readString();
		iconUrl = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(id);
		dest.writeString(title);
		dest.writeString(body);
		dest.writeString(chatId);
		dest.writeInt(chaType);
		dest.writeString(type);
		dest.writeString(iconUrl);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public boolean isReplyable()
	{
		return type.equals(TYPE_CHAT_MESSAGE);
	}

	public boolean hasChatId()
	{
		return !chatId.isEmpty();
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
