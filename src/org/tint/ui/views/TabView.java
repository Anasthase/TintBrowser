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
import android.graphics.Picture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabView extends LinearLayout {
	
    private WebView mWebView;
    private ImageView mClose;
    private TextView mTitle;
    private View mTitleBar;
    private ImageView mImage;
    private OnClickListener mClickListener;
    private boolean mHighlighted;

    public TabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.nav_tab_view, this);

        mClose = (ImageView) findViewById(R.id.closetab);
        mTitle = (TextView) findViewById(R.id.title);
        mTitleBar = findViewById(R.id.titlebar);
        mImage = (ImageView) findViewById(R.id.tab_view);
    }

    protected boolean isClose(View v) {
        return v == mClose;
    }

    protected boolean isTitle(View v) {
        return v == mTitleBar;
    }

    protected boolean isWebView(View v) {
        return v == mImage;
    }

    private void setTitle() {
        if (mWebView == null) return;
        if (mHighlighted) {
            mTitle.setText(mWebView.getUrl());
        } else {
            String txt = mWebView.getTitle();
            if (txt == null) {
                txt = mWebView.getUrl();
            }
            mTitle.setText(txt);
        }
//        if (mTab.isSnapshot()) {
//            setTitleIcon(R.drawable.ic_history_holo_dark);
//        } else if (mTab.isPrivateBrowsingEnabled()) {
//            setTitleIcon(R.drawable.ic_incognito_holo_dark);
//        } else {
//            setTitleIcon(0);
//        }
        setTitleIcon(0);        
    }

    private void setTitleIcon(int id) {
        if (id == 0) {
            mTitle.setPadding(mTitle.getCompoundDrawablePadding(), 0, 0, 0);
        } else {
            mTitle.setPadding(0, 0, 0, 0);
        }
        mTitle.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);
    }

    protected boolean isHighlighted() {
        return mHighlighted;
    }

    protected void setWebView(WebView webview) {
        mWebView = webview;
        
        setTitle();

        Bitmap image = createFromPicture(mWebView.capturePicture());
        if (image != null) {
            mImage.setImageBitmap(image);            
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
        mTitleBar.setOnClickListener(mClickListener);
        mClose.setOnClickListener(mClickListener);
        if (mImage != null) {
            mImage.setOnClickListener(mClickListener);
        }
    }
    
    private Bitmap createFromPicture(Picture p) {
    	Bitmap result = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas c = new Canvas(result);
    	p.draw(c);
    	    	
    	return result;
    }

}
