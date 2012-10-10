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

package org.tint.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchUrlGroup {
	
	private String mName;
	private List<SearchUrlItem> mItems;
	
	public SearchUrlGroup(String name) {
		mName = name;
		mItems = new ArrayList<SearchUrlItem>();
	}	
	
	public String getName() {
		return mName;
	}
	
	public List<SearchUrlItem> getItems() {
		return mItems;
	}
	
	public void addItem(String name, String url) {
		mItems.add(new SearchUrlItem(name, url));
	}
	
	public void sort() {
		Collections.sort(mItems, new Comparator<SearchUrlItem>() {
			@Override
			public int compare(SearchUrlItem lhs, SearchUrlItem rhs) {						
				return lhs.getName().compareTo(rhs.getName());
			}		        	
        });
	}

}
