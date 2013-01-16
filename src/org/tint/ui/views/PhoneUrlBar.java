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

package org.tint.ui.views;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.model.UrlSuggestionCursorAdapter;
import org.tint.model.UrlSuggestionCursorAdapter.QueryBuilderListener;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

public class PhoneUrlBar extends LinearLayout {
	
	public interface OnPhoneUrlBarEventListener {
		
		void onVisibilityChanged(boolean urlBarVisible);
		
		void onUrlValidated();
		
		void onGoStopReloadClicked();
		
		void onMenuVisibilityChanged(boolean isVisible);
		
	}
	
	private Context mContext;
	private Activity mActivity;
	
	private PopupMenu mPopupMenu;
	
	private LinearLayout mTitleLayout;
	private LinearLayout mUrlLayout;
	
	private TextView mTitle;
	private TextView mSubTitle;
	
	private AutoCompleteTextView mUrl;
	
	private ImageView mPrivateBrowsing;
	
	private ImageView mGoStopReload;	
	private ImageView mMenuButton;
	
	private TextWatcher mUrlTextWatcher;
	
	private boolean mIsUrlBarVisible = false;
	private boolean mIsUrlChangedByUser = false;
	
	private OnPhoneUrlBarEventListener mEventListener = null;
	
	private boolean mOverflowMenuShowing;

	public PhoneUrlBar(Context context) {
		this(context, null);
	}

	public PhoneUrlBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PhoneUrlBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext = context;
		
		mActivity = Controller.getInstance().getMainActivity();
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.phone_url_bar, this);
		
		mPrivateBrowsing = (ImageView) v.findViewById(R.id.ImagePrivateBrowsing);
		
		mTitleLayout = (LinearLayout) v.findViewById(R.id.UrlBarTitleLayout);
		mUrlLayout = (LinearLayout) v.findViewById(R.id.UrlBarUrlLayout);
		
		mTitle = (TextView) v.findViewById(R.id.UrlBarTitle);
		mSubTitle = (TextView) v.findViewById(R.id.UrlBarSubTitle);
		
		mUrl = (AutoCompleteTextView) v.findViewById(R.id.UrlBarUrlEdit);
		
		mGoStopReload = (ImageView) v.findViewById(R.id.UrlBarGoStopReload);
		
		mMenuButton = (ImageView) v.findViewById(R.id.MenuButton);
		
		if (ViewConfiguration.get(mContext).hasPermanentMenuKey()) {
			mMenuButton.setVisibility(View.GONE);
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGoStopReload.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mGoStopReload.setLayoutParams(params);
		} else {
			mMenuButton.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					
					if (mActivity == null) {
						mActivity = Controller.getInstance().getMainActivity();
					}
					
					if (mPopupMenu == null) {
			            mPopupMenu = new PopupMenu(mContext, mMenuButton);
			            
			            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {							
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								return mActivity.onOptionsItemSelected(item);
							}
						});
			            
			            mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
							
							@Override
							public void onDismiss(PopupMenu menu) {
								if (menu == mPopupMenu) {
									mOverflowMenuShowing = false;
									if (mEventListener != null) {
										mEventListener.onMenuVisibilityChanged(mOverflowMenuShowing);
									}
								}								
							}
						});
			            
			            if (!mActivity.onCreateOptionsMenu(mPopupMenu.getMenu())) {
			                mPopupMenu = null;
			                return;
			            }
			        }
			        Menu menu = mPopupMenu.getMenu();
			        if (mActivity.onPrepareOptionsMenu(menu)) {
			            mOverflowMenuShowing = true;
			            mPopupMenu.show();
			            
			            if (mEventListener != null) {
							mEventListener.onMenuVisibilityChanged(mOverflowMenuShowing);
						}
			        }
				}
			});
		}
		
		mTitleLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showUrl();
			}
		});
		
		mTitle.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showUrl();
			}
		});
		
		mSubTitle.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showUrl();
			}
		});
		
		String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
    	int[] to = new int[] {R.id.AutocompleteTitle, R.id.AutocompleteUrl};
    	
    	UrlSuggestionCursorAdapter adapter = new UrlSuggestionCursorAdapter(
    			mContext,
    			R.layout.url_autocomplete_line,
    			null,
    			from,
    			to,
    			new QueryBuilderListener() {					
					@Override
					public void onSuggestionSelected(String url) {
						setUrl(url);
						mUrl.setSelection(url.length());
					}
				});
    	
    	adapter.setCursorToStringConverter(new CursorToStringConverter() {			
			@Override
			public CharSequence convertToString(Cursor cursor) {
				String aColumnString = cursor.getString(cursor.getColumnIndex(BookmarksProvider.Columns.URL));
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
		
		mUrl.setDropDownAnchor(R.id.UrlBarContainer);
		
		mGoStopReload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onGoStopReloadClicked();
				}
			}
		});
	}
	
	public void setTitle(String title) {
		mTitle.setText(title);
	}
	
	public void setTitle(int resId) {
		setTitle(mContext.getString(resId));
	}
	
	public void setTitleOnly(String title) {
		mTitle.setText(title);	
				
		mSubTitle.setText(null);
		mSubTitle.setVisibility(View.GONE);
	}
	
	public void setTitleOnly(int resId) {
		setTitleOnly(mContext.getString(resId));
	}
	
	public void setSubtitle(String subtitle) {
		mSubTitle.setText(subtitle);
		
		if ((subtitle == null) ||
				(subtitle.isEmpty())) {
			mSubTitle.setVisibility(View.GONE);
		} else {
			mSubTitle.setVisibility(View.VISIBLE);
		}
	}
	
	public void setSubtitle(int resId) {
		setSubtitle(mContext.getString(resId));
	}
	
	public void showUrl() {
		mTitleLayout.setVisibility(View.GONE);
		mUrlLayout.setVisibility(View.VISIBLE);
		
		mIsUrlBarVisible = true;
		mUrl.requestFocus();
		
		InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(mUrl, InputMethodManager.SHOW_IMPLICIT);
		
		triggerOnUrlBarVisibilityChanged();
	}
	
	public void hideUrl() {
		hideUrl(true);
	}
	
	public void hideUrl(boolean hideKeyboard) {
		mUrlLayout.setVisibility(View.GONE);
		mTitleLayout.setVisibility(View.VISIBLE);
		
		mIsUrlBarVisible = false;
		
		if (hideKeyboard) {
			InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);
		}
		
		triggerOnUrlBarVisibilityChanged();
	}
	
	public boolean isUrlBarVisible() {
		return mIsUrlBarVisible;
	}
	
	public String getUrl() {
		return mUrl.getText().toString();
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
	
	public void showGoStopReloadButton() {
		mGoStopReload.setVisibility(View.VISIBLE);
	}
	
	public void hideGoStopReloadButton() {
		mGoStopReload.setVisibility(View.GONE);
	}
	
	public void setEventListener(OnPhoneUrlBarEventListener listener) {
		mEventListener = listener;
	}
	
	public boolean isMenuShowing() {
		return mOverflowMenuShowing;
	}
	
	public void setPrivateBrowsingIndicator(boolean value) {
		if (value) {
			mPrivateBrowsing.setVisibility(View.VISIBLE);
		} else {
			mPrivateBrowsing.setVisibility(View.GONE);
		}
	}
	
	private void triggerOnUrlBarVisibilityChanged() {
		if (mEventListener != null) {
			mEventListener.onVisibilityChanged(mIsUrlBarVisible);
		}
	}
	
	private void triggerOnUrlValidated() {
		if (mEventListener != null) {
			mEventListener.onUrlValidated();
		}
	}

}
