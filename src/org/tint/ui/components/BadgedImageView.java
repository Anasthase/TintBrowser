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

package org.tint.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BadgedImageView extends ImageView {

	private static final float X_POSITON_RATIO = 52 / 96f;
	private static final float Y_POSITON_RATIO = 64 / 96f;
	
	private int mValue;
	
	private Paint mPaint;
	
	public BadgedImageView(Context context) {
		this(context, null);
	}
	
	public BadgedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BadgedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mValue = 0;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(0xFFC8C8C8);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setFakeBoldText(true);
		mPaint.setTextSize(16 * context.getResources().getDisplayMetrics().density);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
			
		String text;
		if (mValue > 9) {
			text = "*";
		} else {
			text = Integer.toString(mValue);
		}
		
		canvas.drawText(text, X_POSITON_RATIO * getWidth(), Y_POSITON_RATIO * getHeight(), mPaint);
	}
	
	public void setValue(int value) {
		if (mValue != value) {
			mValue = value;
			invalidate();
		}
	}

}
