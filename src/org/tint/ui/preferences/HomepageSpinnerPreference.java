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
import org.tint.utils.Constants;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;

public class HomepageSpinnerPreference extends BaseSpinnerPreference {

	public HomepageSpinnerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected int getTitleArray() {		
		return R.array.HomepageTitles;
	}
	
	@Override
	protected void setEditInputType() {
		mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
	}
	
	@Override
	protected void setSpinnerValueFromPreferences() {
		String currentHomepage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START);
	
		if (currentHomepage.equals(Constants.URL_ABOUT_START)) {
			mSpinner.setSelection(0);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.URL_ABOUT_START);
		} else if (currentHomepage.equals(Constants.URL_ABOUT_BLANK)) {
			mSpinner.setSelection(1);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.URL_ABOUT_BLANK);
		} else {
			mSpinner.setSelection(2);
			mEditText.setEnabled(true);
			mEditText.setText(currentHomepage);					
		}
	}
	
	@Override
	protected void onSpinnerItemSelected(int position) {
		switch(position) {
		case 0:
			mEditText.setText(Constants.URL_ABOUT_START);
			mEditText.setEnabled(false);
			break;
		case 1:
			mEditText.setText(Constants.URL_ABOUT_BLANK);
			mEditText.setEnabled(false);
			break;
		case 2:
			mEditText.setEnabled(true);
			
			if ((mEditText.getText().toString().equals(Constants.URL_ABOUT_START)) ||
					(mEditText.getText().toString().equals(Constants.URL_ABOUT_BLANK))) {					
				mEditText.setText(null);
			}
			
			mEditText.selectAll();
			showKeyboard();
			
			break;
		default:
			mEditText.setText(Constants.URL_ABOUT_START);
			mEditText.setEnabled(false);
			break;
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
			
			switch (mSpinner.getSelectedItemPosition()) {
			case 0:
			case 1:
				editor.putBoolean(Constants.TECHNICAL_PREFERENCE_HOMEPAGE_URL_UPDATE_NEEDED, false);
				break;
			case 2:
				editor.putBoolean(Constants.TECHNICAL_PREFERENCE_HOMEPAGE_URL_UPDATE_NEEDED, true);
				break;
			default:
				editor.putBoolean(Constants.TECHNICAL_PREFERENCE_HOMEPAGE_URL_UPDATE_NEEDED, false);
				break;
			}
			
			editor.commit();
		}
	}

}
