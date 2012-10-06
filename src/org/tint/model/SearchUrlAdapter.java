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

import java.util.List;

import org.tint.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class SearchUrlAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private List<SearchUrlCategory> mData;
	
	public SearchUrlAdapter(Context context, List<SearchUrlCategory> data) {
		mContext = context;
		mData = data;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mData.get(groupPosition).getItems().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return mData.get(groupPosition).getItems().get(childPosition).hashCode();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		TextView item;
        if ((convertView == null) ||
        		(!(convertView instanceof TextView))) {
        	LayoutInflater inflater = LayoutInflater.from(mContext);
        	item = (TextView) inflater.inflate(R.layout.expandable_list_item, null);
        } else {
        	item = (TextView) convertView;
        }
        
        item.setText(((SearchUrlItem) getChild(groupPosition, childPosition)).getName());
		
		return item;
	}

	@Override
	public int getChildrenCount(int groupPosition) {		
		return mData.get(groupPosition).getItems().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return mData.get(groupPosition).hashCode();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
		TextView item;
        if ((convertView == null) ||
        		(!(convertView instanceof TextView))) {
        	LayoutInflater factory = LayoutInflater.from(mContext);
        	item = (TextView) factory.inflate(R.layout.expandable_list_header, null);
        } else {
        	item = (TextView) convertView;
        }
        
        item.setText(((SearchUrlCategory) getGroup(groupPosition)).getName());
        
        return item;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
