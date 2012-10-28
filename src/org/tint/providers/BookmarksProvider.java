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

package org.tint.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;

public class BookmarksProvider extends ContentProvider {	
	
	public static final String AUTHORITY = "org.tint.providers.bookmarksprovider";
	
	private static final String BOOKMARKS_TABLE = "bookmarks";
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tint.bookmarks";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tint.bookmarks";
	
	public static final Uri BOOKMARKS_URI = Uri.parse("content://" + AUTHORITY + "/" + BOOKMARKS_TABLE);
	
	public static class Columns {
		public static final String _ID = "_id";
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String CREATION_DATE = "creation_date";
		public static final String VISITED_DATE = "visited_date";
		public static final String VISITS = "visits";
		public static final String BOOKMARK = "bookmark";
		public static final String IS_FOLDER = "is_folder";
		public static final String PARENT_FOLDER_ID = "parent_folder_id";
		public static final String FAVICON = "favicon";
		public static final String THUMBNAIL = "thumbnail";
	}
	
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "bookmarks.db";
	
	private static final String BOOKMARKS_TABLE_CREATE = "CREATE TABLE " + BOOKMARKS_TABLE + " (" + 
		Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		Columns.TITLE + " TEXT, " +
		Columns.URL + " TEXT, " +
		Columns.VISITS + " INTEGER, " +
		Columns.CREATION_DATE + " LONG, " +
		Columns.VISITED_DATE + " LONG, " +
		Columns.BOOKMARK + " INTEGER, " +
		Columns.IS_FOLDER + " INTEGER NOT NULL DEFAULT 0, " +
		Columns.PARENT_FOLDER_ID + " INTEGER NOT NULL DEFAULT -1, " +
		Columns.FAVICON + " BLOB DEFAULT NULL, " + 
		Columns.THUMBNAIL + " BLOB DEFAULT NULL);";
	
	private static final int BOOKMARKS = 1;
	private static final int BOOKMARKS_BY_ID = 2;
	
	private static final UriMatcher sUriMatcher;
	
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	private boolean mNotifyChanges;
	
	private Context mContext;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, BOOKMARKS_TABLE, BOOKMARKS);
		sUriMatcher.addURI(AUTHORITY, BOOKMARKS_TABLE + "/#", BOOKMARKS_BY_ID);
	}
	
	@Override
	public boolean onCreate() {		
		mContext = getContext();
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		mNotifyChanges = true;
		
		return true;
	}
	
	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		int count = 0;
		
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			count = mDb.delete(BOOKMARKS_TABLE, whereClause, whereArgs);
			break;
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}		
		
		if ((mNotifyChanges) &&
				(count > 0)) {
			mContext.getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			return CONTENT_TYPE;
		case BOOKMARKS_BY_ID:
			return CONTENT_ITEM_TYPE;

		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			long rowId = mDb.insert(BOOKMARKS_TABLE, null, values);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(BOOKMARKS_URI, rowId);
				
				if (mNotifyChanges) {
					mContext.getContentResolver().notifyChange(rowUri, null);
				}
				
				return rowUri;
			}
			
			throw new SQLException("Failed to insert row into " + uri);
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			qb.setTables(BOOKMARKS_TABLE);			
			break;
		case BOOKMARKS_BY_ID:
			qb.setTables(BOOKMARKS_TABLE);
			qb.appendWhere(Columns._ID + " = " + uri.getPathSegments().get(1));
			break;		
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		Cursor c = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			count = mDb.update(BOOKMARKS_TABLE, values, selection, selectionArgs);
			break;
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
						
		if ((mNotifyChanges) &&
				(count > 0)) {
			mContext.getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			int numInserted = 0;
			
			mDb.beginTransaction();
			
			try {
				
				SQLiteStatement insert = mDb.compileStatement(
						"INSERT INTO " + BOOKMARKS_TABLE + "(" +
						Columns.TITLE + ", " + 
						Columns.URL + ", " +
						Columns.VISITS + ", " + 
						Columns.CREATION_DATE + ", " +
						Columns.VISITED_DATE + ", " + 
						Columns.BOOKMARK + ", " +
						Columns.IS_FOLDER + ", " +
						Columns.PARENT_FOLDER_ID + 
						") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				
				for (ContentValues value : values) {
					
					String title = value.getAsString(Columns.TITLE);					
					
					if (!TextUtils.isEmpty(title)) {
					
						String url = value.getAsString(Columns.URL);
						String visits = value.getAsString(Columns.VISITS);
						String creationDate = value.getAsString(Columns.CREATION_DATE);
						String visitedDate = value.getAsString(Columns.VISITED_DATE);
						String bookmark = value.getAsString(Columns.BOOKMARK);
						String isFolder = value.getAsString(Columns.IS_FOLDER);
						String parentFolderId = value.getAsString(Columns.PARENT_FOLDER_ID);
						
						insert.bindString(1, title);
						
						if (!TextUtils.isEmpty(url)) {
							insert.bindString(2, url);
						} else {
							insert.bindNull(2);
						}
						
						if (!TextUtils.isEmpty(visits)) {
							insert.bindString(3, visits);
						} else {
							insert.bindNull(3);
						}
						
						if (!TextUtils.isEmpty(creationDate)) {
							insert.bindString(4, creationDate);
						} else {
							insert.bindNull(4);
						}
						
						if (!TextUtils.isEmpty(visitedDate)) {
							insert.bindString(5, visitedDate);
						} else {
							insert.bindNull(5);
						}
						
						if (!TextUtils.isEmpty(bookmark)) {
							insert.bindString(6, bookmark);
						} else {
							insert.bindString(6, "0");
						}
						
						if (!TextUtils.isEmpty(isFolder)) {
							insert.bindString(7, isFolder);
						} else {
							insert.bindString(7, "0");
						}
						
						if (!TextUtils.isEmpty(parentFolderId)) {
							insert.bindString(8, parentFolderId);
						} else {
							insert.bindString(8, "-1");
						}

						insert.execute();
					}
				}
				
				mDb.setTransactionSuccessful();
				
				mContext.getContentResolver().notifyChange(uri, null);
				
				numInserted = values.length;
			} finally {
				mDb.endTransaction();
			}
			
			return numInserted;
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	public void setNotifyChanges(boolean value) {
		mNotifyChanges = value;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(BOOKMARKS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1: 
				db.execSQL("ALTER TABLE " + BOOKMARKS_TABLE + " ADD " + Columns.IS_FOLDER + " INTEGER NOT NULL DEFAULT 0;");
				db.execSQL("ALTER TABLE " + BOOKMARKS_TABLE + " ADD " + Columns.PARENT_FOLDER_ID + " INTEGER NOT NULL DEFAULT -1;");
				break;
			default: break;
			}
		}		
	}
}
