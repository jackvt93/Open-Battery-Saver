/*
 * Copyright (C) 2014 IUH €yber$oft Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vn.cybersoft.obs.android.provider;

import java.io.File;

import vn.cybersoft.obs.android.utilities.Log;
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
import android.os.Environment;
import android.text.TextUtils;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class DataProvider extends ContentProvider {
	private static final String t = DataProvider.class.getSimpleName();
	
	private OBSDatabaseHelper mOpenHelper;
	
    private static final int TIME_SCHEDULE = 1;
    private static final int TIME_SCHEDULE_ID = 2;
    private static final int OPTIMAL_MODE = 3;
    private static final int OPTIMAL_MODE_ID = 4;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    static {
        sUriMatcher.addURI(DataProviderApi.AUTHORITY, "time_schedules", TIME_SCHEDULE);
        sUriMatcher.addURI(DataProviderApi.AUTHORITY, "time_schedules/#", TIME_SCHEDULE_ID);
        sUriMatcher.addURI(DataProviderApi.AUTHORITY, "optimal_modes", OPTIMAL_MODE);
        sUriMatcher.addURI(DataProviderApi.AUTHORITY, "optimal_modes/#", OPTIMAL_MODE_ID);
    }
	
	public DataProvider() {}

	@Override
	public boolean onCreate() {
		mOpenHelper = new OBSDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case TIME_SCHEDULE:
			qb.setTables(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME);
			break;

		case TIME_SCHEDULE_ID:
			qb.setTables(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME);
			qb.appendWhere(DataProviderApi.TimeSchedulesColumns._ID + "=");
			qb.appendWhere(uri.getLastPathSegment());
			break;
			
		case OPTIMAL_MODE:
			qb.setTables(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME);
			break;
			
		case OPTIMAL_MODE_ID:
			qb.setTables(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME);
            qb.appendWhere(DataProviderApi.OptimalModesColumns._ID + "=");
            qb.appendWhere(uri.getLastPathSegment());
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
								null, sortOrder);

		if (c == null) {
			Log.e(t + ".query: failed");
		} else {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case TIME_SCHEDULE:
            return "vnd.android.cursor.dir/vnd.cybersoft.time_schedules";

        case TIME_SCHEDULE_ID:
        	return "vnd.android.cursor.dir/vnd.cybersoft.time_schedules";
            
        case OPTIMAL_MODE:
        	return "vnd.android.cursor.dir/vnd.cybersoft.optimal_modes";
        	
        case OPTIMAL_MODE_ID:
        	return "vnd.android.cursor.dir/vnd.cybersoft.optimal_modes";

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        long rowId;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TIME_SCHEDULE:
            	rowId = db.insert(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME, null, initialValues);
                break;
            case OPTIMAL_MODE:
                rowId = db.insert(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME, null, initialValues);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL: " + uri);
        }

        Uri uriResult = ContentUris.withAppendedId(DataProviderApi.TimeSchedulesColumns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(uriResult, null);
        return uriResult;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        String primaryKey;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TIME_SCHEDULE:
                count = db.delete(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME, where, whereArgs);
                break;
            case TIME_SCHEDULE_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataProviderApi.TimeSchedulesColumns._ID + "=" + primaryKey;
                } else {
                    where = DataProviderApi.TimeSchedulesColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME, where, whereArgs);
                break;
            case OPTIMAL_MODE:
                count = db.delete(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME, where, whereArgs);
                break;
            case OPTIMAL_MODE_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataProviderApi.OptimalModesColumns._ID + "=" + primaryKey;
                } else {
                    where = DataProviderApi.OptimalModesColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        String id;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TIME_SCHEDULE_ID:
                id = uri.getLastPathSegment();
                count = db.update(OBSDatabaseHelper.TIME_SCHEDULES_TABLE_NAME, values,
                		DataProviderApi.TimeSchedulesColumns._ID + "=" + id,
                        null);
                break;
            case OPTIMAL_MODE_ID:
                id = uri.getLastPathSegment();
                count = db.update(OBSDatabaseHelper.OPTIMAL_MODES_TABLE_NAME, values,
                		DataProviderApi.OptimalModesColumns._ID + "=" + id,
                        null);
                break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        if (Log.LOGV) Log.v("*** notifyChange() id: " + id + " url " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	

}
