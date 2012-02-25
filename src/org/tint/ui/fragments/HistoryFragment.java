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
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.model.HistoryAdapter;
import org.tint.model.BookmarkHistoryItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.UIManager;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int CONTEXT_MENU_OPEN_IN_TAB = Menu.FIRST;
	private static final int CONTEXT_MENU_COPY_URL = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_SHARE_URL = Menu.FIRST + 2;
	private static final int CONTEXT_MENU_DELETE_HISTORY_ITEM = Menu.FIRST + 3;
	
	private View mContainer = null;
	
	private UIManager mUIManager;
	
	private ExpandableListView mListView;
	
	private ListView mGroupList;
	private ListView mChildList;
	
	private TextView mChildHeaderText;
	
	private HistoryAdapter mAdapter;
	
	private HistoryGroupWrapper mGroupAdapter;
	private HistoryChildWrapper mChildAdapter;
	
	private boolean mTwoPaneMode;
	
	private OnCheckedChangeListener mBookmarkStarChangeListener;
	
	public HistoryFragment() {
		mUIManager = Controller.getInstance().getUIManager();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mBookmarkStarChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				long id = (Long) buttonView.getTag();
				BookmarksWrapper.toggleBookmark(getActivity().getContentResolver(), id, isChecked);
				
				if (isChecked) {
					Toast.makeText(getActivity(), R.string.BookmarkAdded, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), R.string.BookmarkRemoved, Toast.LENGTH_SHORT).show();
				}
			}			
		};
		
		mAdapter = new HistoryAdapter(getActivity(),
				mBookmarkStarChangeListener,
				ApplicationUtils.getFaviconSize(getActivity()));
		
		if (mTwoPaneMode) {
			
			mGroupAdapter = new HistoryGroupWrapper(mAdapter);
			mGroupList.setAdapter(mGroupAdapter);
			
			mChildAdapter = new HistoryChildWrapper(mAdapter);
			mChildList.setAdapter(mChildAdapter);
			
			registerForContextMenu(mChildList);
			
		} else {
			mListView.setAdapter(mAdapter);		
			registerForContextMenu(mListView);
		}
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mContainer == null) {
			mContainer = inflater.inflate(R.layout.history_fragment, container, false);
			
			View stub = mContainer.findViewById(R.id.history_group);
			
			if (stub == null) {
				inflateSinglePane();
			} else {
				inflateTwoPane();
			}
		}
		
		return mContainer;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		BookmarkHistoryItem selectedItem = null;
		
		if (mTwoPaneMode) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			
			int group = mChildAdapter.getSelectedGroup();
			int child = info.position;
			
			selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
		} else {
			ExpandableListView.ExpandableListContextMenuInfo info =	(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

			int type = ExpandableListView.getPackedPositionType(info.packedPosition);

			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				int child =	ExpandableListView.getPackedPositionChild(info.packedPosition);

				selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
			}
		}
		
		if (selectedItem != null) {
			BitmapDrawable icon = ApplicationUtils.getApplicationButtonImage(getActivity(), selectedItem.getFavicon());
			if (icon != null) {
				menu.setHeaderIcon(icon);
			}

			menu.setHeaderTitle(selectedItem.getTitle());
			
			menu.add(0, CONTEXT_MENU_OPEN_IN_TAB, 0, R.string.OpenInTab);	
			menu.add(0, CONTEXT_MENU_COPY_URL, 0, R.string.CopyUrl);
			menu.add(0, CONTEXT_MENU_SHARE_URL, 0, R.string.ContextMenuShareUrl);
			menu.add(0, CONTEXT_MENU_DELETE_HISTORY_ITEM, 0, R.string.DeleteHistoryItem);
			
			List<AddonMenuItem> addonsContributions = Controller.getInstance().getAddonManager().getContributedHistoryContextMenuItems(mUIManager.getCurrentWebView());
	        for (AddonMenuItem item : addonsContributions) {
	        	menu.add(0, item.getAddon().getMenuId(), 0, item.getMenuItem());
	        }
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		
		BookmarkHistoryItem selectedItem = null;
		
		if (mTwoPaneMode) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
			
			int group = mChildAdapter.getSelectedGroup();
			int child = info.position;
			
			selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
			
		} else {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

			int type = ExpandableListView.getPackedPositionType(info.packedPosition);

			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				int child =	ExpandableListView.getPackedPositionChild(info.packedPosition);

				selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
			}
		}
		
		if (selectedItem != null) {
			switch (menuItem.getItemId()) {
			case CONTEXT_MENU_OPEN_IN_TAB:
				Intent result = new Intent();
				result.putExtra(Constants.EXTRA_NEW_TAB, true);
				result.putExtra(Constants.EXTRA_URL, selectedItem.getUrl());
				
				getActivity().setResult(Activity.RESULT_OK, result);
				getActivity().finish();
				
				return true;
				
			case CONTEXT_MENU_COPY_URL:
				if (selectedItem != null) {
					ApplicationUtils.copyTextToClipboard(getActivity(), selectedItem.getUrl(), getActivity().getResources().getString(R.string.UrlCopyToastMessage));
				}
				
				return true;
				
			case CONTEXT_MENU_SHARE_URL:
				if (selectedItem != null) {
					ApplicationUtils.sharePage(getActivity(), null, selectedItem.getUrl());						
				}
				
				return true;
				
			case CONTEXT_MENU_DELETE_HISTORY_ITEM:
				BookmarksWrapper.deleteHistoryRecord(getActivity().getContentResolver(), selectedItem.getId());
				return true;
				
			default:
				if (Controller.getInstance().getAddonManager().onContributedHistoryContextMenuItemSelected(
						getActivity(),
						menuItem.getItemId(),
						selectedItem.getTitle(),
						selectedItem.getUrl(),
						mUIManager.getCurrentWebView())) {
					return true;
				} else {
					return super.onContextItemSelected(menuItem);
				}
			}
		}
		
		return super.onContextItemSelected(menuItem);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return BookmarksWrapper.getCursorLoaderForHistory(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		
		if (data != null) {
			
			if (!mTwoPaneMode) {
				if (mAdapter.getGroupCount() > 0) {
					mListView.expandGroup(0, true);
				}
			} else {
				// Select the first group (Today).
				selectGroup(mAdapter.getGroupView(0, false, null, null), 0);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}
	
	private void inflateSinglePane() {
		mTwoPaneMode = false;
		
		mListView = (ExpandableListView) mContainer.findViewById(R.id.HistoryExpandableList);
		
		mListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				openItem(groupPosition, childPosition);
				
				return true;
			}
		});
	}
	
	private void inflateTwoPane() {
		mTwoPaneMode = true;
		
		mChildHeaderText = (TextView) mContainer.findViewById(R.id.history_child_text);
		mGroupList = (ListView) mContainer.findViewById(R.id.history_group_list);
		mChildList = (ListView) mContainer.findViewById(R.id.history_child_list);
		
		mGroupList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectGroup(view, position);
			}
		});
		
		mChildList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openItem(mChildAdapter.getSelectedGroup(), position);
			}
		});
	}
	
	private void openItem(int groupPosition, int childPosition) {
		BookmarkHistoryItem item = (BookmarkHistoryItem) mAdapter.getChild(groupPosition, childPosition);
		Intent result = new Intent();
		result.putExtra(Constants.EXTRA_NEW_TAB, false);
		result.putExtra(Constants.EXTRA_URL, item.getUrl());
		
		getActivity().setResult(Activity.RESULT_OK, result);
		getActivity().finish();
	}
	
	private void selectGroup(View view, int position) {
		CharSequence title = ((TextView) view).getText();
		mChildHeaderText.setText(title);
		
		mChildAdapter.setSelectedGroup(position);
		mGroupList.setItemChecked(position, true);
	}
	
	private abstract class HistoryWrapper extends BaseAdapter {
		
		protected HistoryAdapter mHistoryAdapter;
		
		private DataSetObserver mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                notifyDataSetInvalidated();
            }
        };
        
        public HistoryWrapper(HistoryAdapter adapter) {
        	mHistoryAdapter = adapter;
        	mHistoryAdapter.registerDataSetObserver(mObserver);
        }
	}
	
	private class HistoryGroupWrapper extends HistoryWrapper {

		public HistoryGroupWrapper(HistoryAdapter adapter) {
			super(adapter);
		}

		@Override
		public int getCount() {
			return mAdapter.getGroupCount();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            return mAdapter.getGroupView(position, false, convertView, parent);
		}
	}
	
	private class HistoryChildWrapper extends HistoryWrapper {

		private int mSelectedGroup;
		
		public HistoryChildWrapper(HistoryAdapter adapter) {
			super(adapter);
		}

		@Override
		public int getCount() {
			return mAdapter.getChildrenCount(mSelectedGroup);
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mAdapter.getChildView(mSelectedGroup, position, false, convertView, parent);
		}
		
		public void setSelectedGroup(int groupPosition) {
            mSelectedGroup = groupPosition;
            notifyDataSetChanged();
        }
		
		public int getSelectedGroup() {
			return mSelectedGroup;
		}
		
	}

}
