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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tint.R;
import org.tint.providers.BookmarksProvider;
import org.tint.ui.preferences.IHistoryBookmaksImportListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

public class HistoryBookmarksImportTask extends AsyncTask<String, Integer, String> {

	private Context mContext;
	private IHistoryBookmaksImportListener mListener;

	public HistoryBookmarksImportTask(Context context, IHistoryBookmaksImportListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected String doInBackground(String... params) {

		publishProgress(0, 0, 0);		

		File file = new File(Environment.getExternalStorageDirectory(), params[0]);

		if ((file != null) &&
				(file.exists()) &&
				(file.canRead())) {

			if (file.getName().toLowerCase().endsWith(".json")) {
				return readAsJSON(file);
			} else if (file.getName().toLowerCase().endsWith(".xml")) {
				return readAsXml(file);
			} else {
				return mContext.getString(R.string.HistoryBookmarksImportErrorInvalidFileFormat);
			}

		} else {
			return mContext.getString(R.string.HistoryBookmarksImportFileUnavailable);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onImportProgress(values[0], values[1], values[2]);
	}

	@Override
	protected void onPostExecute(String result) {
		mListener.onImportDone(result);
	}
	
	private String readAsJSON(File file) {
		List<ContentValues> insertValues = null;

		try {			
			insertValues = new ArrayList<ContentValues>();

			publishProgress(1, 0, 0);

			FileInputStream fis = new FileInputStream(file);

			StringBuilder sb = new StringBuilder();
			String line;

			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

				while ((line = reader.readLine()) != null) {					
					sb.append(line);
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				return e.getMessage();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}
			
			JSONObject data = new JSONObject(sb.toString());
			 
			Map<Long, Folder> folders = new HashMap<Long, Folder>();
			
			if (data.has("folders")) {
				JSONArray foldersArray = data.getJSONArray("folders");
				
				int progress = 0;
				int total = foldersArray.length();
				
				for (int i = 0; i < foldersArray.length(); i++) {
					
					publishProgress(3, progress, total);
					
					JSONObject folder = foldersArray.getJSONObject(i);
					
					long id = folder.getLong("id");
					long parentId = folder.getLong("parentId");
					String title = URLDecoder.decode(folder.getString("title"));					
					
					ContentValues values = new ContentValues();
					values.put(BookmarksProvider.Columns.TITLE, title);
					values.put(BookmarksProvider.Columns.BOOKMARK, 0);
					values.put(BookmarksProvider.Columns.IS_FOLDER, 1);
					values.put(BookmarksProvider.Columns.PARENT_FOLDER_ID, -1);
					
					Uri insertionUri = mContext.getContentResolver().insert(BookmarksProvider.BOOKMARKS_URI, values);
					String insertionString = insertionUri.toString();
					
					// Get the new id for the current folder.
					long insertionId = -1;
					try {
						 insertionId = Long.parseLong(insertionString.substring(insertionString.lastIndexOf('/') + 1));
					} catch (NumberFormatException e) {
						insertionId = -1;
					}
					
					// Keep a relation between the id of the folder in the export file, its parent id (in the export file), and its new id.
					folders.put(id, new Folder(insertionId, parentId));
					
					progress++;
				}
				
				publishProgress(4, 0, 0);
				
				// Correct folders parent ids.
				if (!folders.isEmpty()) {
					for (Folder folder : folders.values()) {
						// For each folder previously inserted, check if it had a parent folder in the export file.
						long oldParentId = folder.getOldParentId();
						
						if (oldParentId != -1) {
							// Get the parent folder by its old Id, key of folders map.
							Folder parentFolder = folders.get(oldParentId);
							if (parentFolder != null) {
								
								ContentValues values = new ContentValues();
								values.put(BookmarksProvider.Columns.PARENT_FOLDER_ID, parentFolder.getNewId());
								
								String whereClause = BookmarksProvider.Columns._ID + " = " + folder.getNewId();
								
								mContext.getContentResolver().update(BookmarksProvider.BOOKMARKS_URI, values, whereClause, null);
							}
						}
					}
				}
			}
			
			if (data.has("bookmarks")) {
				JSONArray bookmarksArray = data.getJSONArray("bookmarks");
				
				int progress = 0;
				int total = bookmarksArray.length();
				
				for (int i = 0; i < bookmarksArray.length(); i++) {
					
					publishProgress(5, progress, total);
					
					JSONObject bookmark = bookmarksArray.getJSONObject(i);
					
					long folderId = bookmark.getLong("folderId");
					Folder parentFolder = null;
					if (folderId != -1) {
						parentFolder = folders.get(folderId);
					}
					
					String title = URLDecoder.decode(bookmark.getString("title"));
					String url = URLDecoder.decode(bookmark.getString("url"));
					
					ContentValues values = createContentValues(
							title,
							url,
							bookmark.getInt("visits"),
							bookmark.getLong("visitedDate"),
							bookmark.getLong("creationDate"),
							1);
					
					if (parentFolder != null) {
						values.put(BookmarksProvider.Columns.PARENT_FOLDER_ID, parentFolder.getNewId());
					}
					
					insertValues.add(values);
					
					progress++;
				}
			}
			
			if (data.has("history")) {
				JSONArray historyArray = data.getJSONArray("history");
				
				int progress = 0;
				int total = historyArray.length();
				
				for (int i = 0; i < historyArray.length(); i++) {
					
					publishProgress(6, progress, total);
					
					JSONObject history = historyArray.getJSONObject(i);
					
					String title = URLDecoder.decode(history.getString("title"));
					String url = URLDecoder.decode(history.getString("url"));
					
					ContentValues values = createContentValues(
							title,
							url,
							history.getInt("visits"),
							history.getLong("visitedDate"),
							0,
							0);
					
					insertValues.add(values);
					
					progress++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (JSONException e) {
			e.printStackTrace();
			return e.getMessage();
		}

		if (insertValues != null) {
			publishProgress(7, 0, 0);
			mContext.getContentResolver().bulkInsert(BookmarksProvider.BOOKMARKS_URI, insertValues.toArray(new ContentValues[insertValues.size()]));
		}

		return null;
	}

	private String readAsXml(File file) {
		List<ContentValues> values = null;

		try {				

			publishProgress(1, 0, 0);

			values = new ArrayList<ContentValues>();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();				
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);

			Element root = document.getDocumentElement();

			if ((root != null) &&
					(root.getNodeName().equals("itemlist"))) {

				NodeList itemsList = root.getElementsByTagName("item");

				int progress = 0;
				int total = itemsList.getLength();

				Node item;
				NodeList record;
				Node dataItem;

				for (int i = 0; i < itemsList.getLength(); i++) {

					publishProgress(2, progress, total);

					item = itemsList.item(i);

					if (item != null) {
						record = item.getChildNodes();

						String title = null;
						String url = null;
						int visits = 0;
						long visitedDate = -1;
						long creationDate = -1;
						int bookmark = 0;

						for (int j = 0; j < record.getLength(); j++) {
							dataItem = record.item(j);																

							if ((dataItem != null) &&
									(dataItem.getNodeName() != null)) {

								if (dataItem.getNodeName().equals("title")) {
									title = URLDecoder.decode(getNodeContent(dataItem));										
								} else if (dataItem.getNodeName().equals("url")) {
									url = URLDecoder.decode(getNodeContent(dataItem));
								} else if (dataItem.getNodeName().equals("visits")) {
									try {
										visits = Integer.parseInt(getNodeContent(dataItem));
									} catch (Exception e) {
										visits = 0;
									}
								} else if (dataItem.getNodeName().equals("visiteddate")) {
									try {
										visitedDate = Long.parseLong(getNodeContent(dataItem));
									} catch (Exception e) {
										visitedDate = -1;
									}
								} else if (dataItem.getNodeName().equals("creationdate")) {
									try {
										creationDate = Long.parseLong(getNodeContent(dataItem));
									} catch (Exception e) {
										creationDate = -1;
									}
								} else if (dataItem.getNodeName().equals("bookmark")) {
									try {
										bookmark = Integer.parseInt(getNodeContent(dataItem));
									} catch (Exception e) {
										bookmark = 0;
									}
								}
							}								
						}

						values.add(createContentValues(title, url, visits, visitedDate, creationDate, bookmark));
					}

					progress++;						
				}
			}

		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}

		if (values != null) {
			publishProgress(7, 0, 0);
			mContext.getContentResolver().bulkInsert(BookmarksProvider.BOOKMARKS_URI, values.toArray(new ContentValues[values.size()]));
		}

		return null;
	}
	
	/**
	 * Get the content of a node, why Android does not include Node.getTextContent() ?
	 * @param node The node.
	 * @return The node content.
	 */
	private String getNodeContent(Node node) {
		StringBuffer buffer = new StringBuffer();
		NodeList childList = node.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			Node child = childList.item(i);
			if (child.getNodeType() != Node.TEXT_NODE) {
				continue; // skip non-text nodes
			}
			buffer.append(child.getNodeValue());
		}

		return buffer.toString(); 
	}

	private ContentValues createContentValues(String title, String url, int visits, long visitedDate, long creationDate, int bookmark) {
		ContentValues values = new ContentValues();
		values.put(BookmarksProvider.Columns.TITLE, title);
		values.put(BookmarksProvider.Columns.URL, url);
		values.put(BookmarksProvider.Columns.VISITS, visits);

		if (visitedDate > 0) {
			values.put(BookmarksProvider.Columns.VISITED_DATE, visitedDate);
		} else {
			values.putNull(BookmarksProvider.Columns.VISITED_DATE);
		}

		if (creationDate > 0) {
			values.put(BookmarksProvider.Columns.CREATION_DATE, creationDate);
		} else {
			values.putNull(BookmarksProvider.Columns.CREATION_DATE);
		}

		if (bookmark > 0) {
			values.put(BookmarksProvider.Columns.BOOKMARK, 1);
		} else {
			values.put(BookmarksProvider.Columns.BOOKMARK, 0);
		}

		return values;
	}
	
	/**
	 * Store the new id of a folder, and its
	 * old parent id. E.g. the parent id in the export file.
	 */
	private class Folder {		
		private long mNewId;
		private long mOldParentId;
		
		public Folder(long newId, long oldParentId) {			
			mNewId = newId;
			mOldParentId = oldParentId;
		}
		
		public long getNewId() {
			return mNewId;
		}
		
		public long getOldParentId() {
			return mOldParentId;
		}
	}

}
