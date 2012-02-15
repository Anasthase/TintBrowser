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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class GenericTabListener<T extends Fragment> implements ActionBar.TabListener {

	private Fragment mFragment;
    private Activity mActivity;
    private String mTag;
    private Class<T> mClass;
	
    /** Constructor used each time a new tab is created.
     * @param activity  The host Activity, used to instantiate the fragment
     * @param tag  The identifier tag for the fragment
     * @param clz  The fragment's Class, used to instantiate the fragment
     */
    public GenericTabListener(Activity activity, String tag, Class<T> clz) {
    	this(activity, tag, clz, null);
    }
    
    public GenericTabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
    	mFragment = null;
    	mActivity = activity;
    	mTag = tag;
    	mClass = clz;
    	
    	// Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            ft.detach(mFragment);
            ft.commit();
        }

    }
    
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) { }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
            ft.add(android.R.id.content, mFragment, mTag);
        } else {
            // If it exists, simply attach it in order to show it
            ft.attach(mFragment);
        }
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
        }
	}
}
