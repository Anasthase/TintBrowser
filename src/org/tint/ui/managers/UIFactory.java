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

package org.tint.ui.managers;

import org.tint.R;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.utils.Constants;

import android.content.Context;
import android.preference.PreferenceManager;

public class UIFactory {
	
	public enum UIType {
		TABLET,
		PHONE,
		LEGACY_PHONE
	}
	
	private static boolean isInitialized = false;
	
	private static UIType sUIType;
	
	public static UIType getUIType(Context context) {
		checkInit(context);
		return sUIType;
	}
	
	public static boolean isTablet(Context context) {
		checkInit(context);
		return sUIType == UIType.TABLET;
	}
	
	public static int getMainLayout(Context context) {
		checkInit(context);
		
		switch (sUIType) {
		case TABLET:
			return R.layout.tablet_main_activity;
			
		case PHONE:
			return R.layout.phone_main_activity;
		
		case LEGACY_PHONE:
			return R.layout.legacy_phone_main_activity;
			
		default:
			return R.layout.phone_main_activity;
		}
	}
	
	public static int getMainMenuLayout(Context context) {
		checkInit(context);
		
		switch (sUIType) {
		case TABLET:
			return R.menu.main_activity_menu_tablet;
			
		case PHONE:
		case LEGACY_PHONE:
			return R.menu.main_activity_menu;

		default:
			return R.menu.main_activity_menu;
		}
	}
	
	public static UIManager createUIManager(TintBrowserActivity activity) {
		checkInit(activity);
		
		switch (sUIType) {
		case TABLET:
			return new TabletUIManager(activity);

		case PHONE:
			return new PhoneUIManager(activity);
			
		case LEGACY_PHONE:
			return new LegacyPhoneUIManager(activity);
			
		default:
			return new PhoneUIManager(activity);
		}
	}
	
	private static void init(Context context) {
		String uiTypePref = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFERENCE_UI_TYPE, "AUTO");
		
		if ("AUTO".equals(uiTypePref)) {
			if (context.getResources().getBoolean(R.bool.isTablet)) {
				sUIType = UIType.TABLET;
			} else {
				sUIType = UIType.PHONE;
			}
		} else if ("TABLET".equals(uiTypePref)) {
			sUIType = UIType.TABLET;
		} else if ("LEGACY_PHONE".equals(uiTypePref)) {
			sUIType = UIType.LEGACY_PHONE;
		} else {
			sUIType = UIType.PHONE;
		}
		
		isInitialized = true;
	}
	
	private static void checkInit(Context context) {
		if (!isInitialized) {
			init(context);
		}
	}

}
