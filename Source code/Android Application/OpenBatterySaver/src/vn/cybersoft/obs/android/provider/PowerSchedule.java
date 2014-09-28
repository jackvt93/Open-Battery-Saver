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

import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.CursorLoader;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class PowerSchedule implements Parcelable, DataProviderApi.PowerSchedulesColumns {
	public static final String EXECUTE_SCHEDULE_ACTION = "vn.cybersoft.obs.android.intent.action.EXECUTE_POWER_SCHEDULE"; 
	
    public static final String EXTRA_ID = "power_schedule_id"; 
    
    // This string is used when passing an schedules object through an intent.
    public static final String INTENT_EXTRA = "intent.extra.schedule";
	
	 /**
     * schedule start with an invalid id when it hasn't been saved to the database.
     */
    public static final long INVALID_ID = -1;
    
    // Used when filtering enabled schedules.
    public static final String WHERE_ENABLED = ENABLED + "=1";
	
    public static final String[] QUERY_COLUMNS = {
        _ID, BATTERY_LEVEL, ENABLED, MODE_ID };
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER =
            BATTERY_LEVEL + " ASC";

    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    public static final int ID_INDEX = 0;
    public static final int LEVEL_INDEX = 1;
    public static final int ENABLED_INDEX = 2;
    public static final int MODE_ID_INDEX = 3;
    
    private static final int COLUMN_COUNT = MODE_ID_INDEX + 1;
    
    private static ContentValues createContentValues(PowerSchedule schedule) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        values.put(BATTERY_LEVEL, schedule.level);
        values.put(ENABLED, schedule.enabled ? 1 : 0);
        values.put(MODE_ID, schedule.modeId); 
        return values;
    }
    
    public static Uri getUri(long id) {
    	return ContentUris.withAppendedId(CONTENT_URI, id);
    }
    
    public static long getId(Uri contentUri) { 
    	return ContentUris.parseId(contentUri);
    }
    
    public static CursorLoader getCursorLoader(Context context) {
        return new CursorLoader(context, CONTENT_URI, QUERY_COLUMNS, null, null, DEFAULT_SORT_ORDER);
    }
    
    /**
     * Creates a new time schedule and fills in the given schedule's id.
     */
    public static PowerSchedule addSchedule(ContentResolver contentResolver, PowerSchedule schedule) {
        ContentValues values = createContentValues(schedule);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        schedule.id = getId(uri);
        return schedule;
    }
    
    public static boolean updateSchedule(ContentResolver contentResolver, PowerSchedule schedule) {
        if (schedule.id == OptimalMode.INVALID_ID) return false;
        ContentValues values = createContentValues(schedule);
        long rowsUpdated = contentResolver.update(getUri(schedule.id), values, null, null);
        return rowsUpdated == 1;
    }
    
    public static boolean deleteSchedule(ContentResolver contentResolver, long scheduleId) {
        if (scheduleId == OptimalMode.INVALID_ID) return false;
        int deletedRows = contentResolver.delete(getUri(scheduleId), "", null);
        return deletedRows == 1;
    }
    
    public static PowerSchedule getSchedule(ContentResolver contentResolver, long id) {
        Cursor cursor = contentResolver.query(getUri(id), QUERY_COLUMNS, null, null, null);
        PowerSchedule result = null;
        if (cursor == null) {
            return result;
        }
        try {
            if (cursor.moveToFirst()) {
                result = new PowerSchedule(cursor);
            }
        } finally {
            cursor.close();
        }
        return result;
    }
    
    public static List<PowerSchedule> getSchedules(ContentResolver contentResolver, 
            String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<PowerSchedule> result = new LinkedList<PowerSchedule>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new PowerSchedule(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
    public static void enableSchedule(final ContentResolver contentResolver, final long id, boolean enabled) {
    	PowerSchedule schedule = getSchedule(contentResolver, id);
        if (schedule == null) {
            return;
        }
        ContentValues values = new ContentValues(2);
        values.put(ENABLED, enabled ? 1 : 0);
        contentResolver.update(getUri(id), values, null, null); 
    }
    
	public static final Parcelable.Creator<TimeSchedule> CREATOR = new Parcelable.Creator<TimeSchedule>() {

		@Override
		public TimeSchedule createFromParcel(Parcel p) {
			return new TimeSchedule(p);
		}

		@Override
		public TimeSchedule[] newArray(int size) {
			return new TimeSchedule[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(level); 
		p.writeLong(modeId);
	}
    
	public long id;
	public int level;
	public boolean enabled;
	public long modeId;
	
	public PowerSchedule(Cursor c) {
		id = c.getLong(ID_INDEX);
		level = c.getInt(LEVEL_INDEX);
		enabled = c.getInt(ENABLED_INDEX) >= 1;
		modeId = c.getLong(MODE_ID_INDEX);
	}
	
	public PowerSchedule() {
		id = -1;
		enabled = true;
        modeId = -1;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
        if (!(o instanceof PowerSchedule)) return false;
        final PowerSchedule other = (PowerSchedule) o;
        return id == other.id;
	}
	
	@Override
	public String toString() {
        return "PowerSchedule{" +
                ", id=" + id +
                ", level=" + level +
                ", enabled=" + enabled +
                ", modeId=" + modeId +
                '}';
	}

}
