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
import org.tint.providers.SslExceptionsProvider;
import org.tint.providers.SslExceptionsWrapper;
import org.tint.utils.ApplicationUtils;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class SslExceptionsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private SslExceptionAdapter mAdapter;
	
	private OnCheckedChangeListener mCheckedChangeListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);		
		
		mCheckedChangeListener = new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				long id = (Long) buttonView.getTag();
				SslExceptionsWrapper.toggleSslException(getActivity().getContentResolver(), id, isChecked);
			}
		};
		
		String[] from = new String[] { SslExceptionsProvider.Columns.AUTHORITY };
		int[] to = new int[] { R.id.SslExceptionRow_Title };
		
		mAdapter = new SslExceptionAdapter(getActivity(), R.layout.ssl_exception_row, null, from, to);
		
		setListAdapter(mAdapter);
		
		return view;
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setEmptyText(getString(R.string.SslExceptionEmptyText));
		
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, final long id) {
		super.onListItemClick(l, v, position, id);
		
		ApplicationUtils.showYesNoDialog(
				getActivity(),
				android.R.drawable.ic_dialog_info,
				R.string.RemoveSslExceptionTitle,
				R.string.RemoveSslExceptionMessage,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SslExceptionsWrapper.removeSslException(getActivity().getContentResolver(), id);
					}
					
				});
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		setListShown(false);
		
		return SslExceptionsWrapper.getSslErrorAuthoritiesCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private class SslExceptionAdapter extends SimpleCursorAdapter {
		
		public SslExceptionAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, R.layout.ssl_exception_row, c, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View superView = super.getView(position, convertView, parent);
			
			Cursor c = getCursor();
			
			Switch sw = (Switch) superView.findViewById(R.id.SslExceptionRow_AllowSwitch);
			sw.setTag(c.getLong(c.getColumnIndex(SslExceptionsProvider.Columns._ID)));
			sw.setChecked(c.getInt(c.getColumnIndex(SslExceptionsProvider.Columns.ALLOW)) > 0 ? true : false);
			sw.setOnCheckedChangeListener(mCheckedChangeListener);
			
			int reasons = c.getInt(c.getColumnIndex(SslExceptionsProvider.Columns.REASON));
			
			TextView tv = (TextView) superView.findViewById(R.id.SslExceptionRow_Reasons);
			tv.setText(Html.fromHtml(SslExceptionsWrapper.sslErrorReasonToString(getActivity(), reasons)));
			
			return superView;
		}		
	}

}
