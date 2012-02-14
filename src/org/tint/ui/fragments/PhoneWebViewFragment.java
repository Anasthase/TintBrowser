package org.tint.ui.fragments;

import org.tint.R;
import org.tint.ui.UIManagerProvider;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhoneWebViewFragment extends BaseWebViewFragment {
	
	private boolean mInitialized;
	
	public PhoneWebViewFragment() {
		mUIManager = null;
		mUrlToLoadWhenReady = null;
		mParentView = null;
		mWebView = null;
		
		mInitialized = false;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (!mInitialized) {
			try {
				mUIManager = ((UIManagerProvider) activity).getUIManager();
			} catch (ClassCastException e) {
				Log.e("PhoneWebViewFragment.onAttach()", e.getMessage());
			}
			
			mInitialized = true;
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mParentView == null) {
			mParentView = inflater.inflate(R.layout.phone_webview_fragment, container, false);
		}
		
		return mParentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (mUrlToLoadWhenReady != null) {
			mUIManager.loadUrl(mUrlToLoadWhenReady);
			mUrlToLoadWhenReady = null;
		}
	}
}
