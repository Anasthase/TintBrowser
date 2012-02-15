/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tint.addons.framework;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

public class AddonAction implements Parcelable {
	
	public static final int ACTION_SHOW_TOAST = 0;
	public static final int ACTION_SHOW_DIALOG = 1;
	public static final int ACTION_ASK_USER = 2;
		
	public static final int ACTION_ADD_TAB = 3;
	public static final int ACTION_CLOSE_CURRENT_TAB = 4;
	
	public static final int ACTION_LOAD_URL = 5;
	
	public static final int ACTION_BROWSE_STOP = 6;
	public static final int ACTION_BROWSE_RELOAD = 7;
	public static final int ACTION_BROWSE_FORWARD = 8;
	public static final int ACTION_BROWSE_BACK = 9;
	
	private UUID mId;
	
	private int mAction;
	
	private String mData1;
	private String mData2;
	private String mData3;
	
	public static final Parcelable.Creator<AddonAction> CREATOR = new Parcelable.Creator<AddonAction>() {

		@Override
		public AddonAction createFromParcel(Parcel in) {
			return new AddonAction(in);
		}

		@Override
		public AddonAction[] newArray(int size) {
			return new AddonAction[size];
		}
	};
	
	public AddonAction(int action) {
		this(action, null, null, null);
	}

	public AddonAction(int action, String data1) {
		this(action, data1, null, null);
	}
	
	public AddonAction(int action, String data1, String data2) {
		this(action, data1, data2, null);
	}
	
	public AddonAction(int action, String data1, String data2, String data3) {
		mId = UUID.randomUUID();
		
		mAction = action;
		mData1 = data1;
		mData2 = data2;
		mData3 = data3;
	}
	
	public AddonAction(Parcel in) {
		mId = UUID.fromString(in.readString());
		
		mAction = in.readInt();
		mData1 = in.readString();
		mData2 = in.readString();
		mData3 = in.readString();
	}
	
	public UUID getId() {
		return mId;
	}
	
	public int getAction() {
		return mAction;
	}
	
	public String getData1() {
		return mData1;
	}
	
	public String getData2() {
		return mData2;
	}
	
	public String getData3() {
		return mData3;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId.toString());
		
		dest.writeInt(mAction);
		dest.writeString(mData1);
		dest.writeString(mData2);
		dest.writeString(mData3);
	}
}
