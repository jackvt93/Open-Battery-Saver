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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import vn.cybersoft.obs.android.utilities.Log;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * This class contain battery level info at specific time
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class BatteryTrace implements DataProviderApi.BatteryTracesColumns {
	public static final String TRACE_SERVICE_ACTION = "vn.cybersoft.obs.android.intent.action.BATTERY_TRACE";
	
	
    public static final long INVALID_ID = -1;
	
    public static final String[] QUERY_COLUMNS = {
        _ID, HOUR, MINUTES, LEVEL, DATE };
    
    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    public static final int ID_INDEX = 0;
    public static final int HOUR_INDEX = 1;
    public static final int MINUTES_INDEX = 2;
    public static final int LEVEL_INDEX = 3;
    public static final int DATE_INDEX = 4;
    
    private static final int COUTN_COLUMN = DATE_INDEX + 1;
    
    private static ContentValues createContentValues(BatteryTrace batteryLevelAtTime) {
        ContentValues values = new ContentValues(COUTN_COLUMN);
        values.put(HOUR, batteryLevelAtTime.hour);
        values.put(MINUTES, batteryLevelAtTime.minutes); 
        values.put(LEVEL, batteryLevelAtTime.level);
        values.put(DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(batteryLevelAtTime.date)); 
        return values;
    }
    
    public static Uri getUri(long id) {
    	return ContentUris.withAppendedId(CONTENT_URI, id);
    }
    
    public static long getId(Uri contentUri) { 
    	return ContentUris.parseId(contentUri);
    }
    
    public static BatteryTrace add(ContentResolver contentResolver, BatteryTrace data) {
        ContentValues values = createContentValues(data);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        data.id = getId(uri);
        return data;
    }
    
    public static boolean delete(ContentResolver contentResolver, long id) {
        if (id == INVALID_ID) return false;
        int deletedRows = contentResolver.delete(getUri(id), "", null);
        return deletedRows == 1;
    }
    
    public static BatteryTrace get(ContentResolver contentResolver, long id) {
        Cursor cursor = contentResolver.query(getUri(id), QUERY_COLUMNS, null, null, null);
        BatteryTrace result = null;
        if (cursor == null) {
            return result;
        }
        try {
            if (cursor.moveToFirst()) {
                result = new BatteryTrace(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
    public static String getClosestTraceDate(ContentResolver contentResolver) {
    	Cursor c = contentResolver.query(CONTENT_URI, new String[] { DATE }, null, null, null);
    	c.moveToFirst();
    	String ret = null;
    	try {
    		ret = c.getString(0);
		} finally {
			c.close();
		}
    	return ret; 
    }
    
    public static List<BatteryTrace> getClosestTraceData(ContentResolver contentResolver, int limitDate) {
    	List<String> closestDate = getClosestDate(contentResolver, limitDate);
    	
    	
    	StringBuilder selection = new StringBuilder();
    	
    	for (int i = 0; i < closestDate.size(); i++) {
    		selection.append(DATE + " = ? ");
    		if (i < closestDate.size() - 1) { 
				selection.append("OR ");
			}
		}
    	
    	String[] selectionArgs = closestDate.toArray(new String[closestDate.size()]); 
    	
    	Cursor cursor = contentResolver.query(CONTENT_URI, QUERY_COLUMNS, selection.toString(), selectionArgs, _ID);
        List<BatteryTrace> result = new LinkedList<BatteryTrace>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new BatteryTrace(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
    public static List<String> getClosestDate(ContentResolver contentResolver, int limit) {
    	// nice hack to use group by statement: http://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
    	String selection = "1 = 1) GROUP BY (" + DATE;
    	String sortOrder = "(julianday(DATETIME('NOW')) - julianday(date)) desc LIMIT " + limit;
    	Cursor cursor = contentResolver.query(CONTENT_URI, new String[] { DATE }, selection, null, sortOrder);
    	
    	List<String> result = new LinkedList<String>();
    	
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(0)); 
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        return result;
    }
    
    public static List<BatteryTrace> gets(ContentResolver contentResolver,
            String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<BatteryTrace> result = new LinkedList<BatteryTrace>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new BatteryTrace(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
	public long id;
	public int hour;
	public int minutes;
	public int level;
	public Date date;
	
	public BatteryTrace() {
		id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        date = c.getTime();
	}
	
	public BatteryTrace(Cursor c) {
		id = c.getInt(ID_INDEX);
		hour = c.getInt(HOUR_INDEX);
		minutes = c.getInt(MINUTES_INDEX);
		level = c.getInt(LEVEL_INDEX);
		try {
			date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(c.getString(DATE_INDEX));
		} catch (ParseException e) {
			if (Log.LOGV) {
				Log.e("Error while create BatteryTrace from cursor. Message: " + e.getMessage());
			}
			//e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
        return "BatteryTrace{" +
                ", id=" + id +
                ", hour=" + hour +
                ", minutes=" + minutes +
                ", level=" + level +
                ", date=" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) +
                '}';
	}

}
