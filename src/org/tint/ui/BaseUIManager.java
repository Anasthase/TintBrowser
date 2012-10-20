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

package org.tint.ui;

import java.util.HashMap;
import java.util.UUID;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.model.DownloadItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.tasks.ThumbnailSaver;
import org.tint.ui.activities.BookmarksActivity;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.dialogs.GeolocationPermissionsDialog;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.StartPageFragment;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;
import org.tint.utils.UrlUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView.HitTestResult;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public abstract class BaseUIManager implements UIManager {//, WebViewFragmentListener {
	
	protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
	        new FrameLayout.LayoutParams(
	        ViewGroup.LayoutParams.MATCH_PARENT,
	        ViewGroup.LayoutParams.MATCH_PARENT);
	
	private static final int FOCUS_NODE_HREF = 102;
	
	private FrameLayout mFullscreenContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    
    private GeolocationPermissionsDialog mGeolocationPermissionsDialog;
	
	protected TintBrowserActivity mActivity;	
	protected ActionBar mActionBar;
	protected FragmentManager mFragmentManager;
	
	protected boolean mMenuVisible = false;
	
	private ValueCallback<Uri> mUploadMessage = null;
	
	protected StartPageFragment mStartPageFragment = null;
	
	private Handler mHandler;
	
	public BaseUIManager(TintBrowserActivity activity) {
		mActivity = activity;
		
		mActionBar = mActivity.getActionBar();
		mFragmentManager = mActivity.getFragmentManager();
		
		mGeolocationPermissionsDialog = null;
		
		setupUI();
		
		startHandler();
	}
	
	protected abstract void setupUI();
	
	protected abstract String getCurrentUrl();
	
	protected abstract int getTabCount();
	
	protected abstract BaseWebViewFragment getWebViewFragmentByUUID(UUID fragmentId);
	
	protected abstract void showStartPage(BaseWebViewFragment webViewFragment);
	
	protected abstract void hideStartPage(BaseWebViewFragment webViewFragment);
	
	protected abstract void resetUI();
	
	protected void setApplicationButtonImage(Bitmap icon) {
		BitmapDrawable image = ApplicationUtils.getApplicationButtonImage(mActivity, icon);
		
		if (image != null) {			
			mActionBar.setIcon(image);
		} else {
			mActionBar.setIcon(R.drawable.ic_launcher);
		}
	}		
	
	@Override
	public TintBrowserActivity getMainActivity() {
		return mActivity;
	}
	
	@Override
	public void addTab(boolean loadHomePage, boolean privateBrowsing) {
		if (loadHomePage) {
			addTab(
					PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START),
					false,
					privateBrowsing);
		} else {
			addTab(null, false, privateBrowsing);
		}
	}
	
	@Override
	public void togglePrivateBrowsing() {
		BaseWebViewFragment fragment = getCurrentWebViewFragment();
		if (fragment != null) {
			CustomWebView webView = fragment.getWebView();
			String currentUrl = webView.getUrl();
			
			fragment.setPrivateBrowsing(!webView.isPrivateBrowsingEnabled());			
			fragment.resetWebView();
			
			resetUI();
			loadUrl(currentUrl);
		}
	}
	
	@Override
	public void loadUrl(String url) {
		loadUrl(getCurrentWebViewFragment(), url);
	}
	
	@Override
	public void loadUrl(UUID tabId, String url, boolean loadInCurrentTabIfNotFound) {
		BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
		if (fragment != null) {
			loadUrl(fragment, url);
		} else {
			if (loadInCurrentTabIfNotFound) {
				loadUrl(url);
			}
		}
	}

	@Override
	public void loadRawUrl(UUID tabId, String url,	boolean loadInCurrentTabIfNotFound) {
		BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
		if (fragment != null) {
			fragment.getWebView().loadUrl(url);
		} else {
			if (loadInCurrentTabIfNotFound) {
				getCurrentWebView().loadUrl(url);
			}
		}
	}

	@Override
	public void loadHomePage() {
		loadUrl(PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START));
	}
	
	@Override
	public void loadHomePage(UUID tabId, boolean loadInCurrentTabIfNotFound) {
		loadUrl(
				tabId,
				PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START),
				loadInCurrentTabIfNotFound);
	}

	@Override
	public void loadCurrentUrl() {
		loadUrl(getCurrentUrl());
	}
	
	@Override
	public void openBookmarksActivityForResult() {
		Intent i = new Intent(mActivity, BookmarksActivity.class);
    	mActivity.startActivityForResult(i, TintBrowserActivity.ACTIVITY_BOOKMARKS);
	}
	
	@Override
	public void addBookmarkFromCurrentPage() {
		Intent i = new Intent(mActivity, EditBookmarkActivity.class);
		
		i.putExtra(Constants.EXTRA_ID, (long) -1);
    	i.putExtra(Constants.EXTRA_LABEL, getCurrentWebView().getTitle());
    	i.putExtra(Constants.EXTRA_URL, getCurrentWebView().getUrl());
    	
    	mActivity.startActivity(i);
	}
	
	@Override
	public void shareCurrentPage() {
		WebView webView = getCurrentWebView();
		
		if (webView != null) {
			ApplicationUtils.sharePage(mActivity, webView.getTitle(), webView.getUrl());
		}
	}
	
	@Override
	public void startSearch() {
		WebView webView = getCurrentWebView();
		
		if (webView != null) {
			webView.showFindDialog(null, true);
		}
	}
	
	@Override
	public void clearFormData() {
		WebViewDatabase.getInstance(mActivity).clearFormData();
		getCurrentWebView().clearFormData();
	}
	
	@Override
	public void clearCache() {
		getCurrentWebView().clearCache(true);
	}
	
	@Override
	public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
		getCurrentWebView().setHttpAuthUsernamePassword(host, realm, username, password);
	}
	
	@Override
	public CustomWebView getWebViewByTabId(UUID tabId) {
		BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
		if (fragment != null) {
			return fragment.getWebView();			
		} else {
			return null;
		}
	}
	
	@Override
	public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
		mUploadMessage = uploadMsg;
	}
	
	@Override
	public ValueCallback<Uri> getUploadMessage() {
		return mUploadMessage;
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		if (intent != null) {
			if (Intent.ACTION_VIEW.equals(intent.getAction()) ||
					Intent.ACTION_MAIN.equals(intent.getAction())) {
				// ACTION_VIEW and ACTION_MAIN can specify an url to load.
				String url = intent.getDataString();
				
				if (!TextUtils.isEmpty(url)) {
					if (!isCurrentTabReusable()) {
						addTab(url, false, false);
					} else {
						loadUrl(url);
					}					
				} else {
					// We do not have an url. Open a new tab if there is no tab currently opened,
					// else do nothing.
					if (getTabCount() <= 0) {
						addTab(true, false);
					}
				}
			} else if (Constants.ACTION_BROWSER_CONTEXT_MENU.equals(intent.getAction())) {
				if (intent.hasExtra(Constants.EXTRA_ACTION_ID)) {
					int actionId = intent.getIntExtra(Constants.EXTRA_ACTION_ID, -1);
					
					switch(actionId) {
					case TintBrowserActivity.CONTEXT_MENU_OPEN:
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN);
						} else {
							loadUrl(intent.getStringExtra(Constants.EXTRA_URL));
						}
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_OPEN_IN_NEW_TAB:
						
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN_IN_NEW_TAB, intent.getBooleanExtra(Constants.EXTRA_INCOGNITO, false));
						} else {						
							addTab(intent.getStringExtra(Constants.EXTRA_URL), false, intent.getBooleanExtra(Constants.EXTRA_INCOGNITO, false));
						}
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_OPEN_IN_BACKGROUND:
						
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN_IN_BACKGROUND, intent.getBooleanExtra(Constants.EXTRA_INCOGNITO, false));
						} else {						
							addTab(intent.getStringExtra(Constants.EXTRA_URL), true, intent.getBooleanExtra(Constants.EXTRA_INCOGNITO, false));
						}
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_COPY:
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_COPY);
						} else {
							ApplicationUtils.copyTextToClipboard(mActivity, intent.getStringExtra(Constants.EXTRA_URL), mActivity.getResources().getString(R.string.UrlCopyToastMessage));
						}
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_DOWNLOAD:
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_DOWNLOAD);
						} else {
							DownloadItem item = new DownloadItem(intent.getStringExtra(Constants.EXTRA_URL));

							long id = ((DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
							item.setId(id);

							Controller.getInstance().getDownloadsList().add(item);

							Toast.makeText(mActivity, String.format(mActivity.getString(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
						}
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_SHARE:
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(TintBrowserActivity.CONTEXT_MENU_SHARE);
						} else {
							ApplicationUtils.sharePage(mActivity, null, intent.getStringExtra(Constants.EXTRA_URL));
						}
						break;
						
					default:
						if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1)) {
							requestHrefNode(actionId);
						} else {
							Controller.getInstance().getAddonManager().onContributedContextLinkMenuItemSelected(
									mActivity,
									actionId,
									intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1),
									intent.getStringExtra(Constants.EXTRA_URL),
									getCurrentWebView());
						}
						break;
					}
				}
			}
		} else {
			addTab(true, false);
		}
	}
	
	@Override
	public boolean onKeyBack() {
		if (mCustomView != null) {
			onHideCustomView();
			return true;
		}		
		
		return false;
	}
	
	@Override
	public void onPageFinished(final WebView view, final String url) {
		
		view.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (BookmarksWrapper.urlHasBookmark(mActivity.getContentResolver(), url, view.getOriginalUrl())) {
					Picture p = view.capturePicture();
					
					new ThumbnailSaver(mActivity.getContentResolver(),
							url,
							view.getOriginalUrl(),
							p,
							ApplicationUtils.getBookmarksThumbnailsDimensions(mActivity)).execute();
				}				
			}
		}, 2000);
	}
	
	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		if (view == getCurrentWebView()) {
			setApplicationButtonImage(icon);
		}
	}
	
	@Override
	public void onMainActivityPause() {
		CustomWebView webView = getCurrentWebView();
		if (webView != null) {
			webView.pauseTimers();
		}
	}
	
	@Override
	public void onMainActivityResume() {
		CustomWebView webView = getCurrentWebView();
		if (webView != null) {
			webView.resumeTimers();
		}
	}	
	
	@Override
	public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
		if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
		
		if (requestedOrientation == -1) {
			requestedOrientation = mActivity.getRequestedOrientation();
		}
		
		mOriginalOrientation = mActivity.getRequestedOrientation();
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(mActivity);
        mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        mCustomView = view;
        setFullscreen(true);
        mCustomViewCallback = callback;
        mActivity.setRequestedOrientation(requestedOrientation);
	}
	
	@Override
	public void onHideCustomView() {
		if (mCustomView == null)
            return;
		
        setFullscreen(false);
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        // Show the content view.
        mActivity.setRequestedOrientation(mOriginalOrientation);
	}
	
	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
		if (mGeolocationPermissionsDialog == null) {
			mGeolocationPermissionsDialog = new GeolocationPermissionsDialog(mActivity);
		}
		
		mGeolocationPermissionsDialog.initialize(origin, callback);		
		mGeolocationPermissionsDialog.show();
		
	}

	@Override
	public void onGeolocationPermissionsHidePrompt() {
		if (mGeolocationPermissionsDialog != null) {
			mGeolocationPermissionsDialog.hide();
		}
	}
	
//	@Override
//	public void onFragmentReady(BaseWebViewFragment fragment, String urlToLoadWhenReady) {
//		CustomWebView webView = fragment.getWebView();
//		if (!webView.isPrivateBrowsingEnabled()) {
//			Controller.getInstance().getAddonManager().onTabOpened(mActivity, webView);
//		}
//		
//		if (urlToLoadWhenReady != null) {
//			loadUrl(urlToLoadWhenReady);
//		}
//	}	
	
	@Override
	public void loadUrl(BaseWebViewFragment webViewFragment, String url) {
		if ((url != null) &&
    			(url.length() > 0)) {
			
			if (UrlUtils.isUrl(url)) {
    			url = UrlUtils.checkUrl(url);
    		} else {
    			url = UrlUtils.getSearchUrl(mActivity, url);
    		}
			
			CustomWebView webView = webViewFragment.getWebView();
			
			if (url.equals(Constants.URL_ABOUT_START)) {
//				webView.clearView();
//				webView.clearHistory();
								
				showStartPage(webViewFragment);
				
				// TODO: Check if there is no pb with this.
				// This recreate a new WebView, because i cannot found a way
				// to reset completely (history and display) a WebView.
				webViewFragment.resetWebView();
			} else {
				hideStartPage(webViewFragment);				
				webView.loadUrl(url);
			}
			
			webView.requestFocus();
		}
	}
	
	private void startHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case FOCUS_NODE_HREF:
					String url = (String) msg.getData().get("url");
                    String src = (String) msg.getData().get("src");
                    
                    if (url == "") {
                    	url = src;
                    }
                    
                    if (TextUtils.isEmpty(url)) {
                        break;
                    }
                    
                    switch (msg.arg1) {
                    case TintBrowserActivity.CONTEXT_MENU_OPEN:
                    	loadUrl(url);
                    	break;
                    
					case TintBrowserActivity.CONTEXT_MENU_OPEN_IN_NEW_TAB:
						addTab(url, false, msg.arg2 > 0 ? true : false);
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_OPEN_IN_BACKGROUND:
						addTab(url, true, msg.arg2 > 0 ? true : false);
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_COPY:
						ApplicationUtils.copyTextToClipboard(mActivity, url, mActivity.getResources().getString(R.string.UrlCopyToastMessage));
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_DOWNLOAD:
						DownloadItem item = new DownloadItem(url);

						long id = ((DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
						item.setId(id);

						Controller.getInstance().getDownloadsList().add(item);

						Toast.makeText(mActivity, String.format(mActivity.getString(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
						break;
						
					case TintBrowserActivity.CONTEXT_MENU_SHARE:
						ApplicationUtils.sharePage(mActivity, null, url);
						break;

					default:
						Controller.getInstance().getAddonManager().onContributedContextLinkMenuItemSelected(
								mActivity,
								msg.arg1,
								HitTestResult.SRC_IMAGE_ANCHOR_TYPE,
								url,
								getCurrentWebView());
						break;
					}
                    					
					break;
				default: super.handleMessage(msg);
				}				
			}
			
		};
	}
	
	private void requestHrefNode(int action) {
		requestHrefNode(action, false);
	}
	
	private void requestHrefNode(int action, boolean incognito) {
		WebView webView = getCurrentWebView();
		
		if (webView != null) {
			final HashMap<String, WebView> hrefMap = new HashMap<String, WebView>();		
			hrefMap.put("webview", webView);

			final Message msg = mHandler.obtainMessage(
					FOCUS_NODE_HREF,
					action,
					incognito ? 1 : 0,
							hrefMap);

			webView.requestFocusNodeHref(msg);
		}
	}

	private void setFullscreen(boolean enabled) {
        Window win = mActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (enabled) {
            winParams.flags |=  bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            } else {
                //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        } else {
            winParams.flags &= ~bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        win.setAttributes(winParams);
    }
	
	/**
	 * Check if the current tab can be reused to display an intent request.
	 * A tab is reusable if it is on the user-defined start page.
	 * @return True if the current tab can be reused.
	 */
	private boolean isCurrentTabReusable() {
		String homePageUrl = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START);
		BaseWebViewFragment currentWebViewFragment = getCurrentWebViewFragment();
		CustomWebView currentWebView = getCurrentWebView();
		
		return (currentWebViewFragment != null && currentWebViewFragment.isStartPageShown()) ||
				(currentWebView != null && homePageUrl != null && homePageUrl.equals(currentWebView.getUrl()));
	}
	
	static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }

}
