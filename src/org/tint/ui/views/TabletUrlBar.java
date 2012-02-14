package org.tint.ui.views;

import org.tint.R;
import org.tint.model.UrlSuggestionCursorAdapter;
import org.tint.providers.BookmarksWrapper;

import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

public class TabletUrlBar extends LinearLayout implements UrlBar {
	
	public interface OnTabletUrlBarEventListener {
		
		void onBackClicked();
		
		void onForwardClicked();
		
		void onHomeClicked();
		
		void onBookmarksClicked();
		
		void onGoStopReloadClicked();
		
		void onUrlValidated();
		
	}
	
	private Context mContext;
	
	private AutoCompleteTextView mUrl;	
	
	private ImageView mBack;
	private ImageView mForward;
	
	private ImageView mHome;
	
	private ImageView mGoStopReload;
	private ImageView mBookmarks;
	
	private TextWatcher mUrlTextWatcher;
	
	private boolean mIsUrlChangedByUser = false;
	
	private OnTabletUrlBarEventListener mEventListener = null;

	public TabletUrlBar(Context context) {
		this(context, null);
	}

	public TabletUrlBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public TabletUrlBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext = context;
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.tablet_url_bar, this);
		
		mUrl = (AutoCompleteTextView) v.findViewById(R.id.UrlBarUrlEdit);
		
		mBack = (ImageView) v.findViewById(R.id.UrlBarGoBack);
		mBack.setEnabled(false);
		
		mForward = (ImageView) v.findViewById(R.id.UrlBarGoForward);
		mForward.setEnabled(false);
		
		mHome = (ImageView) v.findViewById(R.id.UrlBarHome);
		
		mBookmarks = (ImageView) v.findViewById(R.id.UrlBarBookmarks);
		
		mGoStopReload = (ImageView) v.findViewById(R.id.UrlBarGoStopReload);
		
		String[] from = new String[] { UrlSuggestionCursorAdapter.URL_SUGGESTION_TITLE, UrlSuggestionCursorAdapter.URL_SUGGESTION_URL };
    	int[] to = new int[] {R.id.AutocompleteTitle, R.id.AutocompleteUrl};
    	
    	UrlSuggestionCursorAdapter adapter = new UrlSuggestionCursorAdapter(mContext, R.layout.url_autocomplete_line, null, from, to);
    	
    	adapter.setCursorToStringConverter(new CursorToStringConverter() {			
			@Override
			public CharSequence convertToString(Cursor cursor) {
				String aColumnString = cursor.getString(cursor.getColumnIndex(UrlSuggestionCursorAdapter.URL_SUGGESTION_URL));
                return aColumnString;
			}
		});
    	
    	adapter.setFilterQueryProvider(new FilterQueryProvider() {		
			@Override
			public Cursor runQuery(CharSequence constraint) {
				if ((constraint != null) &&
						(constraint.length() > 0)) {
					return BookmarksWrapper.getUrlSuggestions(mContext.getContentResolver(),
							constraint.toString());
				} else {
					return BookmarksWrapper.getUrlSuggestions(mContext.getContentResolver(),
							null);
				}
			}
		});
    	
    	mUrl.setThreshold(1);
    	mUrl.setAdapter(adapter);
    	
    	mUrlTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, 	int after) { }
			
			@Override
			public void afterTextChanged(Editable s) {
				mIsUrlChangedByUser = true;		
				mGoStopReload.setImageResource(R.drawable.ic_go);
			}
		};
		
		mUrl.addTextChangedListener(mUrlTextWatcher);
		
		mUrl.setOnKeyListener(new OnKeyListener() {			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {					
					triggerOnUrlValidated();
					return true;
				}
				return false;
			}
		});
		
		mUrl.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				triggerOnUrlValidated();
			}
		});
		
		mBack.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onBackClicked();
				}
			}
		});
		
		mForward.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onForwardClicked();
				}
			}
		});
		
		mHome.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onHomeClicked();
				}
			}
		});
		
		mBookmarks.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onBookmarksClicked();
				}
			}
		});
		
		mGoStopReload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onGoStopReloadClicked();
				}
			}
		});
	}
	
	public void setBackEnabled(boolean enabled) {
		mBack.setEnabled(enabled);
	}
	
	public void setForwardEnabled(boolean enabled) {
		mForward.setEnabled(enabled);
	}
	
	public void setUrl(String url) {
		mUrl.removeTextChangedListener(mUrlTextWatcher);
		mUrl.setText(url);
		mUrl.addTextChangedListener(mUrlTextWatcher);
		mIsUrlChangedByUser = false;
	}
	
	public boolean isUrlChangedByUser() {
		return mIsUrlChangedByUser;
	}
	
	public void setGoStopReloadImage(int resId) {
		mGoStopReload.setImageResource(resId);
	}
	
	public void setEventListener(OnTabletUrlBarEventListener listener) {
		mEventListener = listener;
	}
	
	private void triggerOnUrlValidated() {
		if (mEventListener != null) {
			mEventListener.onUrlValidated();
		}
	}

	@Override
	public String getUrl() {
		return mUrl.getText().toString();
	}

}
