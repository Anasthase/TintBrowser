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

package org.tint.ui.dialogs;

import org.tint.R;

import android.content.Context;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;

public class GeolocationPermissionsDialog extends YesNoRememberDialog {	
	
	private String mOrigin;
	private Callback mCallback;
	
	public GeolocationPermissionsDialog(Context context) {
		super(context);
		
		setTitle(R.string.GeolocationTitle);
		
		setPositiveButtonText(R.string.GeolocationAccept);
		setNegativeButtonText(R.string.GeolocationDecline);
		
		setPositiveButtonListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.invoke(mOrigin, true, mRemember.isChecked());
				}
				
				dismiss();
			}
		});
		
		setNegativeButtonListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.invoke(mOrigin, false, mRemember.isChecked());
				}
				
				dismiss();
			}
		});
	}
	
	public void initialize(String origin, Callback callback) {
		mOrigin = origin;
		mCallback = callback;
		
		mMessageView.setText(String.format(mContext.getString(R.string.GeolocationMessage), mOrigin));
		mRemember.setChecked(false);
	}

}
