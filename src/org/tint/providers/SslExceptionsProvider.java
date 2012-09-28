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
import android.net.Uri;

public class SslExceptionsProvider extends ContentProvider {
	
	public static final String AUTHORITY = "org.tint.providers.sslexceptionsprovider";
	
	private static final String SSL_EXCEPTIONS_TABLE = "sslexceptions";
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tint.sslexceptions";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tint.sslexceptions";
	
	public static final Uri SSL_EXCEPTIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + SSL_EXCEPTIONS_TABLE);
	
	public static class Columns {
		public static final String _ID = "_id";
		public static final String AUTHORITY = "authority";
		public static final String REASON = "reason";
		public static final String ALLOW = "allow";
	}
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sslexceptions.db";
	
	private static final String SSL_EXCEPTION_TABLE_CREATE = "CREATE TABLE " + SSL_EXCEPTIONS_TABLE + " (" + 
		Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		Columns.AUTHORITY + " TEXT NOT NULL, " +
		Columns.REASON + " INTEGER NOT NULL DEFAULT 0, " +
		Columns.ALLOW + " INTEGER NOT NULL DEFAULT 0);";
	
	private static final int EXCEPTIONS = 1;
	private static final int EXCEPTION_BY_ID = 2;
	
	private static final UriMatcher sUriMatcher;
	
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	private Context mContext;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, SSL_EXCEPTIONS_TABLE, EXCEPTIONS);
		sUriMatcher.addURI(AUTHORITY, SSL_EXCEPTIONS_TABLE + "/#", EXCEPTION_BY_ID);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		
		switch (sUriMatcher.match(uri)) {
		case EXCEPTIONS:
			count = mDb.delete(SSL_EXCEPTIONS_TABLE, selection, selectionArgs);
			break;
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}		
		
		mContext.getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case EXCEPTIONS:
			return CONTENT_TYPE;
		case EXCEPTION_BY_ID:
			return CONTENT_ITEM_TYPE;

		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case EXCEPTIONS:
			long rowId = mDb.insert(SSL_EXCEPTIONS_TABLE, null, values);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(SSL_EXCEPTIONS_URI, rowId);
				
				mContext.getContentResolver().notifyChange(rowUri, null);
					
				return rowUri;
			}
			
			throw new SQLException("Failed to insert row into " + uri);
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mContext = getContext();
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (sUriMatcher.match(uri)) {
		case EXCEPTIONS:
			qb.setTables(SSL_EXCEPTIONS_TABLE);			
			break;
		case EXCEPTION_BY_ID:
			qb.setTables(SSL_EXCEPTIONS_TABLE);
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
		case EXCEPTIONS:
			count = mDb.update(SSL_EXCEPTIONS_TABLE, values, selection, selectionArgs);
			break;
			
		default: throw new IllegalArgumentException("Unknown URI " + uri);
		}
						
		mContext.getContentResolver().notifyChange(uri, null);
		
		return count;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SSL_EXCEPTION_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }		
	}

}
