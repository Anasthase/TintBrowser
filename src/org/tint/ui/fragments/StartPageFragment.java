package org.tint.ui.fragments;

import org.tint.R;
import org.tint.model.BookmarkHistoryItem;
import org.tint.model.BookmarksAdapter;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.UIManager;
import org.tint.ui.UIManagerProvider;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class StartPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public interface OnStartPageItemClickedListener {
		public void onStartPageItemClicked(String url);
	}
	
	private GridView mGrid;	
	private BookmarksAdapter mAdapter;
	
	private OnStartPageItemClickedListener mListener = null;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;
	
	private UIManager mUIManager;
	private boolean mInitialized;
	
	public StartPageFragment() {
		mInitialized = false;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (!mInitialized) {
			try {
				mUIManager = ((UIManagerProvider) activity).getUIManager();
			} catch (ClassCastException e) {
				Log.e("StartPageFragment.onAttach()", e.getMessage());
			}
			
			mInitialized = true;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.start_page_fragment, container, false);
		mGrid = (GridView) v.findViewById(R.id.StartPageFragmentGrid);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
		int[] to = new int[] { R.id.StartPageRowTitle, R.id.StartPageRowUrl };
		
		mAdapter = new BookmarksAdapter(
				getActivity(),
				R.layout.start_page_row,
				null,
				from,
				to,
				ApplicationUtils.getBookmarksThumbnailsDimensions(getActivity()),
				R.drawable.browser_thumbnail_light);
		
		mGrid.setAdapter(mAdapter);
		
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (mListener != null) {
					BookmarkHistoryItem item = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);

					if (item != null) {
						mListener.onStartPageItemClicked(item.getUrl());
					}
				}
			}
		});
		
		mGrid.setOnTouchListener(mUIManager);
		
		getLoaderManager().initLoader(0, null, this);		
		
		mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (Constants.PREFERENCE_START_PAGE_LIMIT.equals(key)) {
					getLoaderManager().restartLoader(0, null, StartPageFragment.this);
				}
			}			
		};

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
	}
	
	@Override
	public void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		int limit;
		try {
			limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.PREFERENCE_START_PAGE_LIMIT, "9"));
		} catch (Exception e) {
			limit = 9;
		}
		
		return BookmarksWrapper.getCursorLoaderForStartPage(getActivity(), limit);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	public void setOnStartPageItemClickedListener(OnStartPageItemClickedListener listener) {
		mListener = listener;
	}

}
