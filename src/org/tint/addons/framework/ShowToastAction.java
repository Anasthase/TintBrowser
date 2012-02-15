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

import android.os.Parcel;
import android.widget.Toast;

public class ShowToastAction extends Action {

	private String mMessage;
	private int mToastLenght;
	
	public ShowToastAction(String toastMessage, int toastLength) {
		super(Action.ACTION_SHOW_TOAST);
		
		mMessage = toastMessage;
		
		if ((toastLength == 0) ||
				(toastLength == 1)) {
			mToastLenght = toastLength;
		} else {
			mToastLenght = Toast.LENGTH_SHORT;
		}
	}
	
	public ShowToastAction(Parcel in) {
		super(Action.ACTION_SHOW_TOAST);
		
		mMessage = in.readString();
		mToastLenght = in.readInt();
		
		if ((mToastLenght != Toast.LENGTH_SHORT) &&
				(mToastLenght != Toast.LENGTH_LONG)) {
			mToastLenght = Toast.LENGTH_SHORT;
		}
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	public int getLength() {
		return mToastLenght;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mMessage);
		dest.writeInt(mToastLenght);
	}

}
