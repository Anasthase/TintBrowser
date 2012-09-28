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

import org.tint.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.http.SslError;

public class SslExceptionsWrapper {
	
	public static String[] SSL_EXCEPTIONS_PROJECTION = new String[] {
		SslExceptionsProvider.Columns._ID,
		SslExceptionsProvider.Columns.AUTHORITY,
		SslExceptionsProvider.Columns.REASON,
		SslExceptionsProvider.Columns.ALLOW };
	
	public static final int AUTHORITY_UNKNOWN = 0;
	public static final int AUTHORITY_ALLOWED = 1;
	public static final int AUTHORITY_DISALLOWED = 2;
	
	// Redefinition of SSL errors constants, because those in Android SslError cannot be used for bitmasks.
	private static final int SSL_UNTRUSTED = 1;
	private static final int SSL_IDMISMATCH = 2;
	private static final int SSL_EXPIRED = 4;
	private static final int SSL_NOTYETVALID = 8;
	private static final int SSL_INVALID = 16;
	private static final int SSL_DATE_INVALID = 32;
	
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
	
	public static void setSslException(ContentResolver contentResolver, String authority, int reason, boolean allow) {
		long id = getIdForAuthority(contentResolver, authority);
		
		if (id != -1) {
			String whereClause = SslExceptionsProvider.Columns._ID + " = " + id;
			
			ContentValues values = new ContentValues();
			values.put(SslExceptionsProvider.Columns.REASON, reason);
			values.put(SslExceptionsProvider.Columns.ALLOW, allow ? 1 : 0);
			
			contentResolver.update(SslExceptionsProvider.SSL_EXCEPTIONS_URI, values, whereClause, null);			
		} else {		
			ContentValues values = new ContentValues();
			values.put(SslExceptionsProvider.Columns.AUTHORITY, authority);
			values.put(SslExceptionsProvider.Columns.REASON, reason);
			values.put(SslExceptionsProvider.Columns.ALLOW, allow ? 1 : 0);

			contentResolver.insert(SslExceptionsProvider.SSL_EXCEPTIONS_URI, values);
		}
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
	
	public static String sslErrorReasonToString(Context context, int reason) {
		StringBuilder sb = new StringBuilder();
		
		if ((reason & SSL_UNTRUSTED) == SSL_UNTRUSTED) {
			sb.append(context.getString(R.string.SslUntrusted));
		}
		
		if ((reason & SSL_IDMISMATCH) == SSL_IDMISMATCH) {
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			
			sb.append(context.getString(R.string.SslIDMismatch));
		}
		
		if ((reason & SSL_EXPIRED) == SSL_EXPIRED) {
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			
			sb.append(context.getString(R.string.SslExpired));
		}
		
		if ((reason & SSL_NOTYETVALID) == SSL_NOTYETVALID) {
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			
			sb.append(context.getString(R.string.SslNotYetValid));
		}
		
		if ((reason & SSL_INVALID) == SSL_INVALID) {
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			
			sb.append(context.getString(R.string.SslInvalid));
		}
		
		if ((reason & SSL_DATE_INVALID) == SSL_DATE_INVALID) {
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			
			sb.append(context.getString(R.string.SslDateInvalid));
		}
		
		return sb.toString();
	}
	
	public static int sslErrorToInt(SslError error) {
		int errorCode = 0;
		
		if (error.hasError(SslError.SSL_UNTRUSTED)) {
			errorCode |= SSL_UNTRUSTED;
		}

		if (error.hasError(SslError.SSL_IDMISMATCH)) {
			errorCode |= SSL_IDMISMATCH;
		}

		if (error.hasError(SslError.SSL_EXPIRED)) {
			errorCode |= SSL_EXPIRED;
		}

		if (error.hasError(SslError.SSL_NOTYETVALID)) {
			errorCode |= SSL_NOTYETVALID;
		}
		
		if (error.hasError(SslError.SSL_INVALID)) {
			errorCode |= SSL_INVALID;
		}
		
		if (error.hasError(SslError.SSL_DATE_INVALID)) {
			errorCode |= SSL_DATE_INVALID;
		}
		
		return errorCode;
	}
	
	private static long getIdForAuthority(ContentResolver contentResolver, String authority) {
		long result = -1;
		
		String whereClause = SslExceptionsProvider.Columns.AUTHORITY + " = \"" + authority + "\"";		
		Cursor c = contentResolver.query(SslExceptionsProvider.SSL_EXCEPTIONS_URI, SSL_EXCEPTIONS_PROJECTION, whereClause, null, null);
		
		if (c != null) {
			if (c.moveToFirst()) {
				result = c.getLong(c.getColumnIndex(SslExceptionsProvider.Columns._ID));
			}
			
			c.close();
		}
		
		return result;
	}

}
