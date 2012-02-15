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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class AddonResponse implements Parcelable {
	
	public static final Parcelable.Creator<AddonResponse> CREATOR = new Parcelable.Creator<AddonResponse>() {

		@Override
		public AddonResponse createFromParcel(Parcel in) {
			return new AddonResponse(in);
		}

		@Override
		public AddonResponse[] newArray(int size) {
			return new AddonResponse[size];
		}
	};
	
	private List<AddonAction> mActions;
	
	public AddonResponse() {
		mActions = new ArrayList<AddonAction>();
	}
	
	public AddonResponse(Parcel in) {
		mActions = in.createTypedArrayList(AddonAction.CREATOR);
	}
	
	public List<AddonAction> getActions() {
		return mActions;
	}
	
	public void addAction(AddonAction action) {
		mActions.add(action);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mActions);
	}

}
