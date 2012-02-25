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

package org.tint.ui.tabs;

import org.tint.R;
import org.tint.ui.TabletUIManager;
import org.tint.ui.fragments.TabletWebViewFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;

public class WebViewFragmentTabListener implements ActionBar.TabListener {
	
	private TabletUIManager mUIManager;
	private TabletWebViewFragment mFragment;
	
	private boolean mFragmentAdded;

	public WebViewFragmentTabListener(TabletUIManager uiManager, TabletWebViewFragment fragment) {
		mUIManager = uiManager;
		mFragment = fragment;
		
		mFragmentAdded = false;
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) { }

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction ft) {
		if (mFragment.isStartPageShown()) {
			ft.show(mUIManager.getStartPageFragment());
		} else {
			if (!mFragmentAdded) {
				ft.add(R.id.WebViewContainer, mFragment, null);
				mFragmentAdded = true;
			} else {
				ft.show(mFragment);
			}
		}
		mFragment.onTabSelected(arg0);
		mUIManager.onTabSelected(arg0);
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction ft) {
		if (mFragment.isStartPageShown()) {
			ft.hide(mUIManager.getStartPageFragment());
		} else {
			ft.hide(mFragment);
		}		
	}

}
