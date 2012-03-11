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

package org.tint.ui.activities;

import java.util.List;
import java.util.Random;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.model.DownloadItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.PhoneUIManager;
import org.tint.ui.TabletUIManager;
import org.tint.ui.UIManager;
import org.tint.ui.UIManagerProvider;
import org.tint.ui.preferences.PreferencesActivity;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebIconDatabase;
import android.widget.Toast;

public class TintBrowserActivity extends Activity implements UIManagerProvider {
    
	public static final int ACTIVITY_BOOKMARKS = 0;
	public static final int ACTIVITY_OPEN_FILE_CHOOSER = 1;
	
	public static final int CONTEXT_MENU_OPEN = Menu.FIRST + 10;
	public static final int CONTEXT_MENU_OPEN_IN_NEW_TAB = Menu.FIRST + 11;
	public static final int CONTEXT_MENU_OPEN_IN_BACKGROUND = Menu.FIRST + 12;
	public static final int CONTEXT_MENU_DOWNLOAD = Menu.FIRST + 13;
	public static final int CONTEXT_MENU_COPY = Menu.FIRST + 14;
	public static final int CONTEXT_MENU_SEND_MAIL = Menu.FIRST + 15;
	public static final int CONTEXT_MENU_SHARE = Menu.FIRST + 16;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;
	
	private UIManager mUIManager;
	
	private BroadcastReceiver mDownloadsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onReceivedDownloadNotification(context, intent);
		}
	};
	
	private BroadcastReceiver mPackagesReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Controller.getInstance().getAddonManager().unbindAddons();
			Controller.getInstance().getAddonManager().bindAddons();
		}		
	};
	
	private IntentFilter mPackagesFilter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	boolean isTablet = ApplicationUtils.isTablet(this);
    	
    	if (isTablet) {
    		setTheme(android.R.style.Theme_Holo);
    	} else {
    		setTheme(R.style.ApplicationTheme_Overlay);
    	}
    	
        super.onCreate(savedInstanceState);
        
        if (isTablet) {
        	setContentView(R.layout.tablet_main_activity);
        } else {
        	setContentView(R.layout.phone_main_activity);
        }
        
        getActionBar().setHomeButtonEnabled(true);
        
        getActionBar().addOnMenuVisibilityListener(new OnMenuVisibilityListener() {			
			@Override
			public void onMenuVisibilityChanged(boolean isVisible) {
				mUIManager.onMenuVisibilityChanged(isVisible);
			}
		});
        
        if (isTablet) {
        	mUIManager = new TabletUIManager(this);
        } else {
        	mUIManager = new PhoneUIManager(this);
        }
        
        Controller.getInstance().init(mUIManager, this);        
        Controller.getInstance().getAddonManager().bindAddons();
        
        initializeWebIconDatabase();
        
        mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				mUIManager.onSharedPreferenceChanged(sharedPreferences, key);
				
				// If the user changed the history size, reset the last history truncation date.
				if (Constants.PREFERENCE_HISTORY_SIZE.equals(key)) {
					Editor prefEditor = sharedPreferences.edit();
					prefEditor.putLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, -1);
					prefEditor.commit();
				}
			}
		};
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
		
		mPackagesFilter = new IntentFilter();
		mPackagesFilter.addAction( Intent.ACTION_PACKAGE_ADDED  );
		mPackagesFilter.addAction( Intent.ACTION_PACKAGE_REPLACED );
		mPackagesFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		mPackagesFilter.addCategory( Intent.CATEGORY_DEFAULT ); 
		mPackagesFilter.addDataScheme( "package" );
		
		registerReceiver(mPackagesReceiver, mPackagesFilter);
        
		boolean firstRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.TECHNICAL_PREFERENCE_FIRST_RUN, true);
		if (firstRun) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putBoolean(Constants.TECHNICAL_PREFERENCE_FIRST_RUN, false);
			editor.putInt(Constants.TECHNICAL_PREFERENCE_LAST_RUN_VERSION_CODE, ApplicationUtils.getApplicationVersionCode(this));
			editor.commit();
						
			BookmarksWrapper.fillDefaultBookmaks(
					getContentResolver(),
					getResources().getStringArray(R.array.DefaultBookmarksTitles),
					getResources().getStringArray(R.array.DefaultBookmarksUrls));
			
		} else {
			int currentVersionCode = ApplicationUtils.getApplicationVersionCode(this);
			int savedVersionCode = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.TECHNICAL_PREFERENCE_LAST_RUN_VERSION_CODE, -1);
			
			if (currentVersionCode != savedVersionCode) {
				Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
				editor.putInt(Constants.TECHNICAL_PREFERENCE_LAST_RUN_VERSION_CODE, currentVersionCode);
				editor.commit();
				
				// TODO: Do something on new version.
			}
		}
		
		mUIManager.onNewIntent(getIntent());
    }

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);		
		
		return true;
	} 
			
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);		
		
		menu.setGroupEnabled(
				R.id.MainActivity_DisabledOnStartPageMenuGroup,
				!mUIManager.getCurrentWebViewFragment().isStartPageShown());
		
		boolean privateBrowsing = mUIManager.getCurrentWebView().isPrivateBrowsingEnabled();
		
		menu.findItem(R.id.MainActivity_MenuIncognitoTab).setChecked(privateBrowsing);
		
		menu.removeGroup(R.id.MainActivity_AddonsMenuGroup);
		
		if (!privateBrowsing) {
			List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedMainMenuItems(mUIManager.getCurrentWebView());
			for (AddonMenuItem item : contributedMenuItems) {
				menu.add(R.id.MainActivity_AddonsMenuGroup, item.getAddon().getMenuId(), 0, item.getMenuItem());
			}
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
	    switch (item.getItemId()) { 
	        case R.id.MainActivity_MenuAddTab:
	        	mUIManager.addTab(true, false);
	        	return true;
	        	
	        case R.id.MainActivity_MenuCloseTab:
	        	mUIManager.closeCurrentTab();
	        	return true;
	        	
	        case R.id.MainActivity_MenuAddBookmark:
	        	mUIManager.addBookmarkFromCurrentPage();
	        	return true;
	        	
	        case R.id.MainActivity_MenuBookmarks:
	        	mUIManager.openBookmarksActivityForResult();
	        	return true;
	        	
	        case R.id.MainActivity_MenuIncognitoTab:
	        	mUIManager.togglePrivateBrowsing();
	        	return true;
	        	
	        case R.id.MainActivity_MenuSharePage:
	        	mUIManager.shareCurrentPage();
	        	return true;
	        
	        case R.id.MainActivity_MenuSearch:
	        	mUIManager.startSearch();
	        	return true;
	        	
	        case R.id.MainActivity_MenuPreferences:
	        	i = new Intent(this, PreferencesActivity.class);
	        	startActivity(i);
	        	return true;
	        	
	        default:
	        	if (Controller.getInstance().getAddonManager().onContributedMainMenuItemSelected(
	        			this,
	        			item.getItemId(),
	        			mUIManager.getCurrentWebView())) {
	        		return true;
	        	} else {
	        		return super.onOptionsItemSelected(item);
	        	}
	    }
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if (requestCode == ACTIVITY_BOOKMARKS) {
        	if (resultCode == RESULT_OK) {        	
        		if (intent != null) {
            		Bundle b = intent.getExtras();
            		if (b != null) {
            			if (b.getBoolean(Constants.EXTRA_NEW_TAB)) {
            				mUIManager.addTab(false, false);
            			}
            			
            			mUIManager.loadUrl(b.getString(Constants.EXTRA_URL));
            		}
        		}
        	}
        } else if (requestCode == ACTIVITY_OPEN_FILE_CHOOSER) {
        	if (mUIManager.getUploadMessage() == null) {
        		return;
        	}
        	
        	Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
        	mUIManager.getUploadMessage().onReceiveValue(result);
        	mUIManager.setUploadMessage(null);
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mUIManager.onNewIntent(intent);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mUIManager.onKeyBack()) {
				return true;
			} else {
				return super.onKeyUp(keyCode, event);
			}
		default: return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mUIManager.onMainActivityPause();
		unregisterReceiver(mDownloadsReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mUIManager.onMainActivityResume();
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		
        registerReceiver(mDownloadsReceiver, filter);
	}

	@Override
	protected void onStart() {
//		Controller.getInstance().getAddonManager().bindAddons();
		super.onStart();
	}

	@Override
	protected void onStop() {
		//Controller.getInstance().getAddonManager().unbindAddons();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Controller.getInstance().getAddonManager().unbindAddons();
		WebIconDatabase.getInstance().close();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
		unregisterReceiver(mPackagesReceiver);
		super.onDestroy();
	}	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Do nothing for now, as default implementation mess up with tabs/fragment management.
		// In the future, save and restore tabs.
		//super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Do nothing for now, as default implementation mess up with tabs/fragment management.
		// In the future, save and restore tabs.
		//super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onActionModeFinished(ActionMode mode) {		
		super.onActionModeFinished(mode);
		mUIManager.onActionModeFinished(mode);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {		
		super.onActionModeStarted(mode);
		mUIManager.onActionModeStarted(mode);
	}

	@Override
	public UIManager getUIManager() {
		return mUIManager;
	}
	
	/**
     * Initialize the Web icons database.
     */
    private void initializeWebIconDatabase() {
        
    	final WebIconDatabase db = WebIconDatabase.getInstance();
    	db.open(getDir("icons", 0).getPath());   
    }
	
	private void showNotification(String notificationTitle, String title, String message) {
        Notification notification =  new Notification(android.R.drawable.stat_sys_download_done, notificationTitle, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent notificationIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, 0);
        
        notification.setLatestEventInfo(this, title, message, contentIntent);
        
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(new Random().nextInt(), notification);
    }
	
	private void onReceivedDownloadNotification(Context context, Intent intent) {
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			DownloadItem item = Controller.getInstance().getDownloadItemById(id);
			
			if (item != null) {
				// This is one of our downloads.
                final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Query query = new Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);
                
                if (cursor.moveToFirst()) {
                	int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    
                    int status = cursor.getInt(statusIndex);
                    
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    	String localUri = cursor.getString(localUriIndex);
                    	
                    	Toast.makeText(context, String.format(getString(R.string.DownloadComplete), localUri), Toast.LENGTH_SHORT).show();                    	
                    	Controller.getInstance().getDownloadsList().remove(item);
                    	
                    	showNotification(getString(R.string.DownloadComplete), item.getFileName(), getString(R.string.DownloadComplete));
                    	
                    } else if (status == DownloadManager.STATUS_FAILED) {
                    	int reason = cursor.getInt(reasonIndex);
                        
                        String message;
                        switch (reason) {
                        case DownloadManager.ERROR_FILE_ERROR:
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:                                    
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                message = getString(R.string.DownloadErrorDisk);
                                break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                message = getString(R.string.DownloadErrorHttp);
                                break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                message = getString(R.string.DownloadErrorRedirection);
                                break;
                        default:
                                message = getString(R.string.DownloadErrorUnknown);
                                break;
                        }
                        
                        Toast.makeText(context, String.format(getString(R.string.DownloadFailedWithErrorMessage), message), Toast.LENGTH_SHORT).show();
                        Controller.getInstance().getDownloadsList().remove(item);
                    }

                }
			}
		} else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
			Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(i);
		}
	}
}