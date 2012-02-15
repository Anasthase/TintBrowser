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

public abstract class BaseAddonAction implements Parcelable {
	
	public static final BaseAddonAction EMPTY_STATE = new BaseAddonAction() {};
	
	private final Parcelable mSuperState;
	
	private BaseAddonAction() {
        mSuperState = null;
    }
	
	protected BaseAddonAction(Parcelable superState) {
        if (superState == null) {
            throw new IllegalArgumentException("superState must not be null");
        }
        mSuperState = superState != EMPTY_STATE ? superState : null;
    }
	
	protected BaseAddonAction(Parcel source) {
        Parcelable superState = source.readParcelable(null);
        mSuperState = superState != null ? superState : EMPTY_STATE;
    }
	
	final public Parcelable getSuperState() {
        return mSuperState;
    }
	
	@Override
	public int describeContents() {
        return 0;
    }
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSuperState, flags);
	}
	
	public static final Parcelable.Creator<BaseAddonAction> CREATOR	= new Parcelable.Creator<BaseAddonAction>() {

		public BaseAddonAction createFromParcel(Parcel in) {
			Parcelable superState = in.readParcelable(null);
			if (superState != null) {
				throw new IllegalStateException("superState must be null");
			}
			return EMPTY_STATE;
		}

		public BaseAddonAction[] newArray(int size) {
			return new BaseAddonAction[size];
		}
	};

}
