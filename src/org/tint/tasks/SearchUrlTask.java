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

package org.tint.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tint.R;
import org.tint.model.SearchUrlCategory;

import android.content.Context;
import android.os.AsyncTask;

public class SearchUrlTask extends AsyncTask<Void, Integer, String> {

	private Context mContext;
	private ISearchUrlTaskListener mListener;
	
	private Map<String, SearchUrlCategory> mResults;
	
	public SearchUrlTask(Context context, ISearchUrlTaskListener listener) {
		super();
		
		mContext = context;
		mListener = listener;
		
		mResults = new HashMap<String, SearchUrlCategory>();
	}
	
	public List<SearchUrlCategory> getResults() {
		List<SearchUrlCategory> result = new ArrayList<SearchUrlCategory>();
		for (SearchUrlCategory item : mResults.values()) {
			item.sort();
			result.add(item);
		}
		
		Collections.sort(result, new Comparator<SearchUrlCategory>() {
			@Override
			public int compare(SearchUrlCategory lhs, SearchUrlCategory rhs) {						
				return lhs.getName().compareTo(rhs.getName());
			}		        	
        });
		
		return result;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		
		publishProgress(0);
		
		String message = null;
		HttpURLConnection c = null;		
		
		try {
			URL url = new URL("http://anasthase.github.com/TintBrowser/search-engines.json");
			c = (HttpURLConnection) url.openConnection();
			
			c.connect();
			
			int responseCode = c.getResponseCode();
			
			if (responseCode == 200) {
				StringBuilder sb = new StringBuilder();
				
				InputStream is = c.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				String line;
		        while ((line = reader.readLine()) != null) {
		        	sb.append(line);
		        }
		        
		        publishProgress(1);
		        
		        JSONArray jsonArray = new JSONArray(sb.toString());
		        
		        for (int i = 0; i < jsonArray.length(); i++) {
		        	JSONObject jsonObject = jsonArray.getJSONObject(i);
		        	
		        	String categoryName = jsonObject.getString("category");
		        	
		        	SearchUrlCategory category = mResults.get(categoryName);
		        	if (category == null) {
		        		category = new SearchUrlCategory(categoryName);
		        		mResults.put(categoryName, category);
		        	}
		        	
		        	JSONArray engines = jsonObject.getJSONArray("engines");
		        	for (int j = 0; j < engines.length(); j++) {
		        		JSONObject engine = engines.getJSONObject(j);
		        		
		        		category.addItem(
		        				engine.getString("name"),
		        				engine.getString("url"));
		        	}
		        }
		        
			} else {
				message = String.format(mContext.getString(R.string.SearchUrlBadResponseCodeMessage), Integer.toString(responseCode));
			}
			
		} catch (MalformedURLException e) {
			message = e.getMessage();
		} catch (IOException e) {
			message = e.getMessage();
		} catch (JSONException e) {
			message = e.getMessage();
		} finally {
			if (c != null) {
				c.disconnect();
			}
		}
		
		return message;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mListener != null) {
			mListener.onProgress(values[0]);
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (mListener != null) {
			mListener.onDone(result);
		}
	}
	
	public interface ISearchUrlTaskListener {
		
		void onProgress(int step);		
		void onDone(String result);
		
	}

}
