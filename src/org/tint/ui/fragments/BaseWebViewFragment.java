package org.tint.ui.fragments;

import org.tint.R;
import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebChromeClient;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.components.CustomWebViewClient;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public abstract class BaseWebViewFragment extends Fragment {
	
	protected UIManager mUIManager;
	protected View mParentView;
	protected CustomWebView mWebView;
	
	protected String mUrlToLoadWhenReady = null;
	
	private Animation mShowAnimation;
	private Animation mHideAnimation;
	
	private boolean mIsStartPageShown = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		mShowAnimation = new AlphaAnimation(0, 1);
		mShowAnimation.setDuration(250);
		
		mHideAnimation = new AlphaAnimation(1, 0);
		mHideAnimation.setDuration(250);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mWebView = (CustomWebView) mParentView.findViewById(R.id.WebView);
		
		mWebView.setWebChromeClient(new CustomWebChromeClient(mUIManager));
		mWebView.setWebViewClient(new CustomWebViewClient(mUIManager));
		
		mWebView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}		
		});
		
		mWebView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}			
		});
		
		mWebView.setOnTouchListener(mUIManager);
	}
	
	@Override
	public void onPause() {
		mWebView.onPause();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		mWebView.onResume();
		super.onResume();
	}

	public CustomWebView getWebView() {
		if (mWebView == null) {
			// Force fragment creation if we do not have the WebView yet.
			this.getFragmentManager().executePendingTransactions();
		}
		
		return mWebView;
	}
	
	public void requestUrlToLoadWhenReady(String url) {
		mUrlToLoadWhenReady = url;
	}	
	
	public boolean isStartPageShown() {
		return mIsStartPageShown;
	}
	
	public void setStartPageShown(boolean value) {
		mIsStartPageShown = value;
	}

}
