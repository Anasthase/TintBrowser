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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tint.R;
import org.tint.providers.BookmarksProvider;
import org.tint.ui.preferences.IHistoryBookmaksExportListener;
import org.tint.utils.IOUtils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

public class HistoryBookmarksExportTask extends AsyncTask<Cursor, Integer, String> {

	private Context mContext;
	private IHistoryBookmaksExportListener mListener;

	public HistoryBookmarksExportTask(Context context, IHistoryBookmaksExportListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected String doInBackground(Cursor... params) {

		publishProgress(0, 0, 0);

		String cardState = IOUtils.checkCardState(mContext);
		if (cardState != null) {
			return cardState;
		}

		return writeAsJSON(params);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onExportProgress(values[0], values[1], values[2]);
	}

	@Override
	protected void onPostExecute(String result) {
		mListener.onExportDone(result);
	}

	/**
	 * Get a string representation of the current date / time in a format suitable for a file name.
	 * @return A string representation of the current date / time.
	 */
	private String getNowForFileName() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);

		return sdf.format(c.getTime());
	}
	
	private String writeAsJSON(Cursor... params) {
		try {
			String fileName = mContext.getString(R.string.ApplicationName) + "-" + getNowForFileName() + ".json";

			File file = new File(Environment.getExternalStorageDirectory(), fileName);		
			FileWriter writer = new FileWriter(file);			
			
			FoldersJSONArray foldersArray = new FoldersJSONArray();
			BookmarksJSONArray bookmarksArray = new BookmarksJSONArray();
			HistoryJSONArray historyArray = new HistoryJSONArray();
			
			Cursor c = params[0];
			if (c.moveToFirst()) {

				int current = 0;
				int total = c.getCount();

				int idIndex = c.getColumnIndex(BookmarksProvider.Columns._ID);
				int titleIndex = c.getColumnIndex(BookmarksProvider.Columns.TITLE);
				int urlIndex = c.getColumnIndex(BookmarksProvider.Columns.URL);
				int creationDateIndex = c.getColumnIndex(BookmarksProvider.Columns.CREATION_DATE);
				int visitedDateIndex = c.getColumnIndex(BookmarksProvider.Columns.VISITED_DATE);
				int visitsIndex = c.getColumnIndex(BookmarksProvider.Columns.VISITS);
				int bookmarkIndex = c.getColumnIndex(BookmarksProvider.Columns.BOOKMARK);
				int folderIndex = c.getColumnIndex(BookmarksProvider.Columns.IS_FOLDER);
				int parentfolderIdIndex = c.getColumnIndex(BookmarksProvider.Columns.PARENT_FOLDER_ID);

				while (!c.isAfterLast()) {

					publishProgress(1, current, total);

					boolean isFolder = c.getInt(folderIndex) > 0 ? true : false;					

					if (isFolder) {						
						String title = c.getString(titleIndex);
						title = title != null ? URLEncoder.encode(title, "UTF-8") : "";
						
						foldersArray.add(
								title,
								c.getLong(idIndex),
								c.getLong(parentfolderIdIndex));
						
					} else {
						boolean isBookmark = c.getInt(bookmarkIndex) > 0 ? true : false;
						
						String title = c.getString(titleIndex);
						title = title != null ? URLEncoder.encode(title, "UTF-8") : "";

						String url = c.getString(urlIndex);
						url = url != null ? URLEncoder.encode(url, "UTF-8") : "";
						
						if (isBookmark) {
							bookmarksArray.add(
									c.getLong(parentfolderIdIndex),
									title,
									url,
									c.getLong(creationDateIndex),
									c.getLong(visitedDateIndex),
									c.getInt(visitsIndex));							
						} else {
							historyArray.add(
									title,
									url,
									c.getLong(visitedDateIndex),
									c.getInt(visitsIndex));
						}
					}

					current++;
					c.moveToNext();
				}
			}
			
			JSONObject output = new JSONObject();
			output.put("folders", foldersArray);
			output.put("bookmarks", bookmarksArray);
			output.put("history", historyArray);
			
			writer.write(output.toString(1));
			
			writer.flush();
			writer.close();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return null;
	}
	
	private class FoldersJSONArray extends JSONArray {
		
		public void add(String title, long id, long parentId) throws JSONException {
			JSONObject item = new JSONObject();
			item.put("title", title);
			item.put("id", id);
			item.put("parentId", parentId);
			
			this.put(item);
		}
		
	}
	
	private class BookmarksJSONArray extends JSONArray {
		
		public void add(long folderId, String title, String url, long creationDate, long visitedDate, int visits) throws JSONException {
			JSONObject item = new JSONObject();
			item.put("folderId", folderId);
			item.put("title", title);
			item.put("url", url);
			item.put("creationDate", creationDate);
			item.put("visitedDate", visitedDate);
			item.put("visits", visits);

			this.put(item);
		}
		
	}
	
	private class HistoryJSONArray extends JSONArray  {

		public void add(String title, String url, long visitedDate, int visits) throws JSONException {
			JSONObject item = new JSONObject();
			item.put("title", title);
			item.put("url", url);
			item.put("visitedDate", visitedDate);
			item.put("visits", visits);

			this.put(item);
		}
	}

}
