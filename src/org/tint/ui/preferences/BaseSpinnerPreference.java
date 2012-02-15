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
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public abstract class BaseSpinnerPreference extends DialogPreference {
	
	private Context mContext;
	private View mContainer;
	
	protected Spinner mSpinner;
	protected EditText mEditText;

	public BaseSpinnerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContainer = inflater.inflate(R.layout.spinner_preference, null);
		
		return mContainer;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		
		mSpinner = (Spinner) mContainer.findViewById(R.id.SpinnerPreferenceSpinner);
		mEditText = (EditText) mContainer.findViewById(R.id.SpinnerPreferenceEdit);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), getTitleArray(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		mSpinner.setAdapter(adapter);
		
		setEditInputType();
		setSpinnerValueFromPreferences();
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				onSpinnerItemSelected(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
	    	editor.putString(getKey(), mEditText.getText().toString());
	    	editor.commit();
		}
	}
	
	protected void showKeyboard() {
		InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
	}
	
	protected void hideKeyboard() {
		InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

	protected abstract int getTitleArray();
	
	protected abstract void setEditInputType();
	
	protected abstract void setSpinnerValueFromPreferences();
	
	protected abstract void onSpinnerItemSelected(int position); 
	
}
