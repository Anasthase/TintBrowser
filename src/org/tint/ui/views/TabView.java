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

package org.tint.ui.views;

import org.tint.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabView extends LinearLayout {

	private ImageView mCloseView;
	private TextView mTitleView;
	private View mTitleBar;
	private ImageView mImage;
	
	private int mImageWidth;
	private int mImageHeight;
	
	private int mFaviconSize;
	
	private String mTitle;
	private boolean mSelected;
	
	private OnClickListener mClickListener;

	public TabView(Context context) {
		super(context);
		init(context);
	}

	public TabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}    

	public TabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.tab_view, this);

		mTitle = null;
		mSelected = false;
		
		mCloseView = (ImageView) findViewById(R.id.closetab);
		mTitleView = (TextView) findViewById(R.id.title);
		mTitleBar = findViewById(R.id.titlebar);
		mImage = (ImageView) findViewById(R.id.tab_view);
		
		float density = context.getResources().getDisplayMetrics().density;
		
		mImageWidth = (int) (200 * density);
		mImageHeight = (int) (120 * density);
		mFaviconSize = (int) (32 * density);
	}

	public boolean isClose(View v) {
		return v == mCloseView;
	}

	public boolean isTitle(View v) {
		return v == mTitleBar;
	}

	public boolean isWebView(View v) {
		return v == mImage;
	}
	
	public void setImage(Picture picture) {
		Bitmap bm = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0xFFFFFFFF);
		canvas.drawRect(0, 0, mImageWidth, mImageHeight, p);
		
		if (picture != null) {
			float scale = mImageWidth / (float) picture.getWidth();
			canvas.scale(scale, scale);

			picture.draw(canvas);			
		}
		
		mImage.setImageBitmap(bm);
	}
	
	public void setImageResource(int resource) {
		mImage.setImageResource(resource);
	}
	
	public void setTitle(String title) {
		mTitle = title;
		updateTitle();
	}
	
	public void setTitle(int title) {
		mTitle = getResources().getString(title);
		updateTitle();
	}
	
	public void setSelected(boolean selected) {
		mSelected = selected;
		updateTitle();
	}
	
	public void setFavicon(Bitmap icon) {
		BitmapDrawable bd;
		if (icon != null) {
			bd = new BitmapDrawable(Bitmap.createScaledBitmap(icon, mFaviconSize, mFaviconSize, false));
		} else {
			bd = null;
		}
		
		mTitleView.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
		mTitleBar.setOnClickListener(mClickListener);
		mCloseView.setOnClickListener(mClickListener);
		if (mImage != null) {
			mImage.setOnClickListener(mClickListener);
		}
	}
	
	private void updateTitle() {
		if (mTitle != null) {
			if (mSelected) {
				mTitleView.setText(Html.fromHtml(String.format("<b>%s</b>", mTitle)));
			} else {
				mTitleView.setText(mTitle);
			}
		} else {
			mTitleView.setText(null);
		}
	}

}
