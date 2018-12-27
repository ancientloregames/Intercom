package com.ancientlore.intercom.data;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class Chat
{
	@PrimaryKey(autoGenerate = true)
	public long id;

	@ColumnInfo
	public int type;

	@ColumnInfo
	public int status;

	@ColumnInfo
	public long name;

	@ColumnInfo
	public long lastMsg;

	@ColumnInfo
	public long newMsgCount;

	@ColumnInfo
	public long timeCreated;

	@ColumnInfo
	public long timeUpdated;

	@ColumnInfo
	public long pinned;
}
