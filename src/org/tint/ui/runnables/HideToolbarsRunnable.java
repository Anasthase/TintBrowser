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

package org.tint.ui.runnables;

import org.tint.ui.LegacyPhoneUIManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HideToolbarsRunnable implements Runnable {

	private LegacyPhoneUIManager mUIManager;
	private int mDuration;
	
	private boolean mDisabled;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if ((!mDisabled) &&
					(mUIManager != null)) {
				mUIManager.hideToolbars();
			}
		}
	};
	
	public HideToolbarsRunnable(LegacyPhoneUIManager uiManager, int duration) {
		mUIManager = uiManager;
		mDuration = duration;
		
		mDisabled = false;
	}
	
	public void disable() {
		mDisabled = true;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(mDuration);
			mHandler.sendEmptyMessage(0);
		} catch (InterruptedException e) {
			Log.d("HideToolbarsRunnable", e.getMessage());
			mHandler.sendEmptyMessage(0);
		}
		
	}

}
