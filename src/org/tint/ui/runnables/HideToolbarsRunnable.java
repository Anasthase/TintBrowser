package org.tint.ui.runnables;

import org.tint.ui.PhoneUIManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HideToolbarsRunnable implements Runnable {

	private PhoneUIManager mUIManager;
	private int mDuration;
	
	private boolean mDisabled;
	
	private Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			if ((!mDisabled) &&
					(mUIManager != null)) {
				mUIManager.hideToolbars();
			}
		}
	};
	
	public HideToolbarsRunnable(PhoneUIManager uiManager, int duration) {
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
			mhandler.sendEmptyMessage(0);
		} catch (InterruptedException e) {
			Log.d("HideToolbarsRunnable", e.getMessage());
			mhandler.sendEmptyMessage(0);
		}
		
	}

}
