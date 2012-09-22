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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public class SslExceptionsWrapper {
	
	public static String[] SSL_EXCEPTIONS_PROJECTION = new String[] {
		SslExceptionsProvider.Columns._ID,
		SslExceptionsProvider.Columns.AUTHORITY,
		SslExceptionsProvider.Columns.ALLOW };
	
	public static final int AUTHORITY_UNKNOWN = 0;
	public static final int AUTHORITY_ALLOWED = 1;
	public static final int AUTHORITY_DISALLOWED = 2;
	
	public static CursorLoader getSslErrorAuthoritiesCursorLoader(Context context) {
		return new CursorLoader(context, SslExceptionsProvider.SSL_EXCEPTIONS_URI, SSL_EXCEPTIONS_PROJECTION, null, null, null);
	}
	
	public static int getStatusForAuthority(ContentResolver contentResolver, String authority) {
		int result = AUTHORITY_UNKNOWN;
		
		String whereClause = SslExceptionsProvider.Columns.AUTHORITY + " = \"" + authority + "\"";
		
		Cursor c = contentResolver.query(SslExceptionsProvider.SSL_EXCEPTIONS_URI, SSL_EXCEPTIONS_PROJECTION, whereClause, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				if (c.getInt(c.getColumnIndex(SslExceptionsProvider.Columns.ALLOW)) > 0) {
					result = AUTHORITY_ALLOWED;
				} else {
					result = AUTHORITY_DISALLOWED;
				}
			}
			
			c.close();
		}
		
		return result;
	}
	
	public static void setSslException(ContentResolver contentResolver, String authority, boolean allow) {
		ContentValues values = new ContentValues();
		values.put(SslExceptionsProvider.Columns.AUTHORITY, authority);
		values.put(SslExceptionsProvider.Columns.ALLOW, allow ? 1 : 0);
		
		contentResolver.insert(SslExceptionsProvider.SSL_EXCEPTIONS_URI, values);
	}
	
	public static void toggleSslException(ContentResolver contentResolver, long id, boolean allow) {
		String whereClause = SslExceptionsProvider.Columns._ID + " = " + id;
		
		ContentValues values = new ContentValues();
		values.put(SslExceptionsProvider.Columns.ALLOW, allow ? 1 : 0);
		
		contentResolver.update(SslExceptionsProvider.SSL_EXCEPTIONS_URI, values, whereClause, null);
	}
	
	public static void removeSslException(ContentResolver contentResolver, long id) {
		String whereClause = SslExceptionsProvider.Columns._ID + " = " + id;
		
		contentResolver.delete(SslExceptionsProvider.SSL_EXCEPTIONS_URI, whereClause, null);
	}

}
