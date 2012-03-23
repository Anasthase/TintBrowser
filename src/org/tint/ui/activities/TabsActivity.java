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

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.UIManager;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.views.TabScroller;
import org.tint.ui.views.TabScroller.OnRemoveListener;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabsActivity extends Activity {

	private int mOrientation;
	
	private UIManager mUIManager;
	private TabScroller mTabScroller;
	
	private List<BaseWebViewFragment> mTabs;
	
	private int mTabWidth;
	private int mTabHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs_activity);
		
		mTabWidth = (int) getResources().getDimension(R.dimen.nav_tab_width);
		mTabHeight = (int) getResources().getDimension(R.dimen.nav_tab_height);
		
		mUIManager = Controller.getInstance().getUIManager();		
		mTabs = mUIManager.getTabs();
		
		mOrientation = getResources().getConfiguration().orientation;
		
		mTabScroller = (TabScroller) findViewById(R.id.TabsScroller);
		
		mTabScroller.setOrientation(mOrientation == Configuration.ORIENTATION_LANDSCAPE
                ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
		
		mTabScroller.setAdapter(new TabsAdapter(this), mUIManager.getCurrentTabIndex());
        
		mTabScroller.setOnRemoveListener(new OnRemoveListener() {
			@Override
			public void onRemovePosition(int position) {
				
			}        	
        });
		
		
	}

	private class TabsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		
		public TabsAdapter(Context context) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.nav_tab_view, null, false);
			}
			
			BaseWebViewFragment current = mTabs.get(position);
			WebView webView = current.getWebView();
			
			TextView tv = (TextView) convertView.findViewById(R.id.title);
			tv.setText(webView.getTitle());
			
			ImageView iv = (ImageView) convertView.findViewById(R.id.tab_view);
			iv.setImageBitmap(current.getScreenShot(mTabWidth, mTabHeight));
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					mUIManager.setTabIndex(position);
					TabsActivity.this.finish();
				}
			});
			
			return convertView;
		}
		
	}
	
}
