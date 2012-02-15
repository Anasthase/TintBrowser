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

package org.tint.ui.fragments;

import java.util.List;

import org.tint.R;
import org.tint.addons.Addon;
import org.tint.controllers.Controller;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class AddonsFragment extends ListFragment {
	
	private List<Addon> mAddons;
	
	private AddonsAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		mAddons = Controller.getInstance().getAddonManager().getAddons();
						
		mAdapter = new AddonsAdapter(getActivity());
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Bundle args = new Bundle();
		args.putInt(AddonDetailsFragment.EXTRA_ADDON_ID, position);
		
		PreferenceActivity activity = (PreferenceActivity) getActivity();
		activity.startPreferencePanel(AddonDetailsFragment.class.getName(), args, 0, mAddons.get(position).getName(), this, 0);
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mAdapter.notifyDataSetChanged();
	}



	private class AddonsAdapter extends ArrayAdapter<Addon> {
		
		private LayoutInflater mInflater;
		
		public AddonsAdapter(Context context) {
			super(context, 0, 0, mAddons);
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			
			if (convertView == null) {
                view = mInflater.inflate(R.layout.addon_row, parent, false);
            } else {
                view = convertView;
            }
			
			final Addon addon = mAddons.get(position);
			
			TextView title = (TextView) view.findViewById(R.id.AddonName);
			title.setText(addon.getName());
			
			TextView desc = (TextView) view.findViewById(R.id.AddonShortDesc);
			desc.setText(addon.getShortDescription());
			
			Switch enabled = (Switch) view.findViewById(R.id.AddonEnabled);
			enabled.setChecked(addon.isEnabled());
			
			enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					addon.setEnabled(isChecked);
				}
			});
			
			return view;
		}

		@Override
		public int getCount() {
			return mAddons.size();
		}
		
	}	

}
