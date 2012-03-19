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
import org.tint.model.TabItem;
import org.tint.ui.PhoneUIManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class TabsActivity extends Activity {
	
	private float mHeightFactor = 0.6f;
    private float mWidthFactor = 0.6f;
    
    private int mThumbHeight;
    private int mThumbWidth;
	
	private PhoneUIManager mUiManager;
	private Gallery mGallery;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs_activity);
		
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		switch (display.getRotation()) {
		case Surface.ROTATION_0:
        case Surface.ROTATION_180:
                mHeightFactor = 0.6f;
                mWidthFactor = 0.6f;
                break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
                mHeightFactor = 0.5f;
                mWidthFactor = 0.6f;
                break;   
        default:
                break;

		}
		
		mThumbHeight = (int) (mHeightFactor * getResources().getDisplayMetrics().heightPixels);
        mThumbWidth = (int) (mWidthFactor * getResources().getDisplayMetrics().widthPixels);
		
		mUiManager = (PhoneUIManager) Controller.getInstance().getUIManager();
		
		mGallery = (Gallery) findViewById(R.id.TabsGallery);
		mGallery.setSpacing(15);
		mGallery.setUnselectedAlpha(0.5f);
		
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				mUiManager.selectTab(position);
				finish();
			}
			
		});
		
		TabsAdapter adapter = new TabsAdapter(this, mUiManager.getTabs());
		mGallery.setAdapter(adapter);
		mGallery.setSelection(mUiManager.getSelectedTabIndex());
	}
	
	private class TabsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<TabItem> mTabs;
		
		public TabsAdapter(Context context, List<TabItem> tabs) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTabs = tabs;
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.tab_view, null, false);
			}
			
			TextView title = (TextView) convertView.findViewById(R.id.TabTile);
			ImageView thumbnail = (ImageView) convertView.findViewById(R.id.TabThumbnail);
			
			TabItem item = mTabs.get(position);
			
			title.setText(item.getTitle());
			
			Picture p = item.getThumbnail();
			
			Bitmap bm = Bitmap.createBitmap(mThumbWidth, mThumbHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bm);
			
			if (p != null) {
				p.draw(canvas);
			} else {
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(0xFFFFFFFF);
                canvas.drawRect(new RectF(0, 0, mThumbWidth, mThumbHeight), paint);

			}
			
			thumbnail.setImageBitmap(bm);
			
			return convertView;
		}
		
	}

}
