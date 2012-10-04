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

package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.PreferencesUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
	
	private int mDefaultValue;
	private int mMinValue;
	private int mMaxValue;
	private int mStepValue;

	private boolean mSpacedSymbol;
	
	private String mSymbol;
	
	private TextView mTitle;
	private TextView mSummary;
	private TextView mValue;
	private SeekBar mSeekBar;
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
			
			mMinValue = a.getInt(R.styleable.SeekBarPreference_minValue, 0);
			mMaxValue = a.getInt(R.styleable.SeekBarPreference_maxValue, 10);
			mStepValue = a.getInt(R.styleable.SeekBarPreference_stepValue, 1);
			
			mSpacedSymbol = a.getBoolean(R.styleable.SeekBarPreference_spacedSymbol, false);
			
			if (mMaxValue <= mMinValue) {
				mMaxValue = mMinValue + 1;
			}
			
			if (mDefaultValue < mMinValue) {
				mDefaultValue = mMinValue;
			}
			
			if (mStepValue <= 0) {
				mStepValue = 1;
			}
			
			mMinValue = Math.round(mMinValue / mStepValue);
			mMaxValue = Math.round(mMaxValue / mStepValue);
			
			mDefaultValue = a.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0);
			
			mSymbol = a.getString(R.styleable.SeekBarPreference_symbol);
			
			a.recycle();
		}
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View v = inflater.inflate(R.layout.seekbar_preference, null);
		
		mTitle = (TextView) v.findViewById(R.id.SeekBarPreferenceTitle);
		mTitle.setText(getTitle());
		
		mSummary = (TextView) v.findViewById(R.id.SeekBarPreferenceSummary);
		if (!TextUtils.isEmpty(getSummary())) {
			mSummary.setText(getSummary());
		} else {
			mSummary.setVisibility(View.GONE);
		}
		
		mValue = (TextView) v.findViewById(R.id.SeekBarPreferenceValue);
		
		mSeekBar = (SeekBar) v.findViewById(R.id.SeekBarPreferenceSeekBar);
		mSeekBar.setMax(mMaxValue - mMinValue);
		
		int currentValue = getBoundedValue(PreferencesUtils.getConvertedIntPreference(getContext(), getKey(), mDefaultValue));		
		currentValue = currentValue - mMinValue;
		
		mSeekBar.setProgress(currentValue);
		updateValue(currentValue, false);
		mSeekBar.setOnSeekBarChangeListener(this);
		
		return v;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		updateValue(progress, true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }	

	private int getBoundedValue(int value) {
		
		value = Math.round(value / mStepValue);
		
		if (value < mMinValue) {
			value = mMinValue;
		}
		
		if (value > mMaxValue) {
			value = mMaxValue;
		}
		
		return value;
	}
	
	private void updateValue(int value, boolean save) {			
		
		value = (value + mMinValue) * mStepValue;
		
		mValue.setText(String.format((mSpacedSymbol ? "%s " : "%s") + mSymbol, value));
		
		if (save) {
			PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(getKey(), value).commit();
		}
	}
	
}
