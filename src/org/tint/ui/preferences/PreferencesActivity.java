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

import java.util.List;

import org.tint.R;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
    public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_headers, target);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
            finish();
			return true;
		default: return super.onContextItemSelected(item);
		}
	}
	
}
