/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.addons.framework;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleAction extends BaseAddonAction {

	public static final int ACTION_CLOSE_CURRENT_TAB = 0;
	public static final int ACTION_BROWSE_STOP = 1;
	public static final int ACTION_BROWSE_RELOAD = 2;
	public static final int ACTION_BROWSE_FORWARD = 3;
	public static final int ACTION_BROWSE_BACK = 4;
	
	private int mAction;
	
	public static final Parcelable.Creator<SimpleAction> CREATOR = new Parcelable.Creator<SimpleAction>() {

		@Override
		public SimpleAction createFromParcel(Parcel in) {
			return new SimpleAction(in);
		}

		@Override
		public SimpleAction[] newArray(int size) {
			return new SimpleAction[size];
		}
	};
	
	private SimpleAction(Parcelable superState) {
        super(superState);
    }	

	private SimpleAction(Parcel in) {
		super(in);
		mAction = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(mAction);
	}

}
