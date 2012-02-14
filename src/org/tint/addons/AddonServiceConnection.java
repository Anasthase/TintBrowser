package org.tint.addons;

import org.tint.addons.framework.AddonResponse;
import org.tint.addons.framework.IAddon;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class AddonServiceConnection implements ServiceConnection {

	public interface AddonServiceConnectionListener {
		void onServiceConnected();
	}
	
	private Intent mIntent;
	private IAddon mAddon;
	
	private boolean mIsBinded;
	
	private AddonServiceConnectionListener mListener;
	
	public AddonServiceConnection(Intent intent) {
		mIntent = intent;
		mAddon = null;
		mIsBinded = false;
		mListener = null;
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder boundService) {
		mAddon = IAddon.Stub.asInterface(boundService);
		mIsBinded = true;

		try {			
			mAddon.onBind();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (mListener != null) {
			mListener.onServiceConnected();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		
		try {
			mAddon.onUnbind();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		mAddon = null;
		mIsBinded = false;
	}
	
	public void setAddonServiceConnectionListener(AddonServiceConnectionListener listener) {
		mListener = listener;
	}
	
	public int getCallbacks() {
		try {
			return mAddon.getCallbacks();
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public String getName() {
		try {
			return mAddon.getName();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getShortDescription() {
		try {
			return mAddon.getShortDescription();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDescription() {
		try {
			return mAddon.getDescription();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getEMail() {
		try {
			return mAddon.getEMail();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getWebsite() {
		try {
			return mAddon.getWebsite();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onPageStarted(String url) {
		try {
			return mAddon.onPageStarted(url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onPageFinished(String url) {
		try {
			return mAddon.onPageFinished(url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedMainMenuItem() {
		try {
			return mAddon.getContributedMainMenuItem();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onContributedMainMenuItemSelected(String currentTitle, String currentUrl) {
		try {
			return mAddon.onContributedMainMenuItemSelected(currentTitle, currentUrl);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedLinkContextMenuItem() {
		try {
			return mAddon.getContributedLinkContextMenuItem();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onContributedLinkContextMenuItemSelected(int hitTestResult, String url) {
		try {
			return mAddon.onContributedLinkContextMenuItemSelected(hitTestResult, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedHistoryBookmarksMenuItem() {
		try {
			return mAddon.getContributedHistoryBookmarksMenuItem();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onContributedHistoryBookmarksMenuItemSelected() {
		try {
			return mAddon.onContributedHistoryBookmarksMenuItemSelected();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedBookmarkContextMenuItem() {
		try {
			return mAddon.getContributedBookmarkContextMenuItem();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onContributedBookmarkContextMenuItemSelected(String title, String url) {
		try {
			return mAddon.onContributedBookmarkContextMenuItemSelected(title, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedHistoryContextMenuItem() {
		try {
			return mAddon.getContributedHistoryContextMenuItem();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onContributedHistoryContextMenuItemSelected(String title, String url) {
		try {
			return mAddon.onContributedHistoryContextMenuItemSelected(title, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public AddonResponse onUserAnswerQuestion(String questionId, boolean positiveAnswer) {
		try {
			return mAddon.onUserAnswerQuestion(questionId, positiveAnswer);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void showAddonPreferenceActivity() {
		try {
			mAddon.showAddonPreferenceActivity();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}	
	
	public Intent getIntent() {
		return mIntent;
	}
	
	public boolean isBinded() {
		return mIsBinded;
	}

}
