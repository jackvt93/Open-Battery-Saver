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

import vn.cybersoft.obs.android.utilities.DeviceUtils;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * @author Luan Vu
 *
 */
public class OptimalMode implements /*Parcelable */ DataProviderApi.OptimalModesColumns {
    public static final String EXTRA_ID = "optimal_mode_id"; 
	
    public static final long INVALID_ID = -1;
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER =
            _ID + " ASC";

    public static final String[] QUERY_COLUMNS = {
        _ID, NAME, DESC, CAN_EDIT, SCREEN_BRIGHTNESS,
        SCREEN_TIMEOUT, VIBRATE, WIFI, BLUETOOTH, SYNC, HAPTIC_FEEDBACK };

    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    public static final int MODE_ID_INDEX = 0;
    public static final int MODE_NAME_INDEX = 1;
    public static final int MODE_DESC_INDEX = 2;
    public static final int MODE_CAN_EDIT_INDEX = 3;
    public static final int MODE_SCREEN_BRIGHTNESS_INDEX = 4;
    public static final int MODE_SCREEN_TIMEOUT_INDEX = 5;
    public static final int MODE_VIBRATE_INDEX = 6;
    public static final int MODE_WIFI_INDEX = 7;
    public static final int MODE_BLUETOOTH_INDEX = 8;
    public static final int MODE_SYNC_INDEX = 9;
    public static final int MODE_HAPTIC_FEEDBACK_INDEX = 10;
    
    private static final int COUTN_COLUMN = MODE_HAPTIC_FEEDBACK_INDEX + 1;
    
    private static ContentValues createContentValues(OptimalMode mode) {
        ContentValues values = new ContentValues(COUTN_COLUMN);
        values.put(NAME, mode.name);
        values.put(DESC, mode.desc);
        values.put(CAN_EDIT, mode.canEdit ? 1 : 0);
        values.put(SCREEN_BRIGHTNESS, mode.screenBrightness);
        values.put(SCREEN_TIMEOUT, mode.screenTimeout);
        values.put(VIBRATE, mode.vibrate ? 1 : 0);
        values.put(WIFI, mode.wifi ? 1 : 0);
        values.put(BLUETOOTH, mode.bluetooth ? 1 : 0);
        values.put(SYNC, mode.sync ? 1 : 0);
        values.put(HAPTIC_FEEDBACK, mode.hapticFeedback ? 1 : 0);
        return values;
    }
    
    public static long getModeId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }
    
    public static Uri getContentUriForId(long modeId) {
    	return ContentUris.withAppendedId(CONTENT_URI, modeId);
    }
    
    public static OptimalMode addMode(ContentResolver contentResolver, OptimalMode mode) {
        ContentValues values = createContentValues(mode);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        mode.id = getModeId(uri);
        return mode;
    }
    
    public static boolean updateMode(ContentResolver contentResolver, OptimalMode mode) {
        if (mode.id == OptimalMode.INVALID_ID) return false;
        ContentValues values = createContentValues(mode);
        long rowsUpdated = contentResolver.update(getContentUriForId(mode.id), values, null, null);
        return rowsUpdated == 1;
    }

    public static boolean deleteMode(ContentResolver contentResolver, long modeId) {
        if (modeId == OptimalMode.INVALID_ID) return false;
        int deletedRows = contentResolver.delete(getContentUriForId(modeId), "", null);
        return deletedRows == 1;
    }
    
    /**
     * Get mode cursor loader for all modes.
     * 
     * @param context to query the database.
     * @return cursor loader with all the optimal modes.
     */
    public static CursorLoader getModesCursorLoader(Context context) {
        return new CursorLoader(context, DataProviderApi.OptimalModesColumns.CONTENT_URI, 
        		QUERY_COLUMNS, null, null, DEFAULT_SORT_ORDER);
    }
    
    /**
     * Get mode by id.
     * 
     * @param contentResolver
     * @param modeId
     * @return mode if found, null otherwise
     */
    public static OptimalMode getMode(ContentResolver contentResolver, long modeId) {
        Cursor cursor = contentResolver.query(getContentUriForId(modeId), QUERY_COLUMNS, null, null, null);
        OptimalMode result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new OptimalMode(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
    /**
     * Get all optimal modes given conditions.
     * 
     * @param contentResolver to perform the query on.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @return list of optimal modes matching where clause or empty list if none found.
     */
    public static List<OptimalMode> getModes(ContentResolver contentResolver,
            String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<OptimalMode> result = new LinkedList<OptimalMode>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new OptimalMode(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    
    private static String generalModeName() {
    	Cursor c = null;
    	
    	try {
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	return "";
    }
	
/*	public static final Parcelable.Creator<OptimalMode> CREATOR = new Parcelable.Creator<OptimalMode>() {

		@Override
		public OptimalMode createFromParcel(Parcel p) {
			return new OptimalMode(p);
		}

		@Override
		public OptimalMode[] newArray(int size) {
			return new OptimalMode[size];
		}

	};*/
	
/*	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(id);
		p.writeString(name);
		p.writeString(desc);
		p.writeInt(canEdit ? 1 : 0);
		p.writeInt(screenBrightness);
		p.writeInt(screenTimeout);
		p.writeInt(vibrate ? 1 : 0);
		p.writeInt(wifi ? 1 : 0);
		p.writeInt(bluetooth ? 1 : 0);
		p.writeInt(sync ? 1 : 0);
		p.writeInt(hapticFeedback ? 1 : 0);
	}*/
	
	public long id;
	public String name;
	public String desc;
	public boolean canEdit;
	public int screenBrightness;
	public int screenTimeout;
	public boolean vibrate;
	public boolean wifi;
	public boolean bluetooth;
	/* not use yet */ 
	public boolean mobileData;
	public boolean sync;
	public boolean hapticFeedback;
	
/*    public OptimalMode(Parcel p) {
    	id = p.readInt();
    	name = p.readString();
    	desc = p.readString();
    	canEdit = p.readInt() == 1;
    	screenBrightness = p.readInt();
    	screenTimeout = p.readInt();
    	vibrate = p.readInt() == 1;
    	wifi = p.readInt() == 1;
    	bluetooth = p.readInt() == 1;
    	sync = p.readInt() == 1;
    	hapticFeedback = p.readInt() == 1;
    }*/
    
    public OptimalMode() {
    	id = -1;
    	canEdit = true;
    	screenBrightness = DeviceUtils.getScreenBrightness();
    	screenTimeout = DeviceUtils.getScreenTimeoutInMillis();
    	vibrate = true;
    	wifi = true;
    	bluetooth = true;
    	sync = true;
    	hapticFeedback = true;
    }
	
	public OptimalMode(Cursor c) {
		id = c.getInt(MODE_ID_INDEX);
		name = c.getString(MODE_NAME_INDEX);
		desc = c.getString(MODE_DESC_INDEX);
		canEdit = c.getInt(MODE_CAN_EDIT_INDEX) >= 1;
		screenBrightness = c.getInt(MODE_SCREEN_BRIGHTNESS_INDEX);
		screenTimeout = c.getInt(MODE_SCREEN_TIMEOUT_INDEX);
		vibrate = c.getInt(MODE_VIBRATE_INDEX) >= 1;
		wifi = c.getInt(MODE_WIFI_INDEX) >= 1;
		bluetooth = c.getInt(MODE_BLUETOOTH_INDEX) >= 1;
		sync = c.getInt(MODE_SYNC_INDEX) >= 1;
		hapticFeedback = c.getInt(MODE_HAPTIC_FEEDBACK_INDEX) >= 1;
	}
	
	public OptimalMode(int id, String name, String desc, boolean canEdit,
			int screenBrightness, int screenTimeout, boolean vibrate,
			boolean wifi, boolean bluetooth, boolean mobileData, boolean sync,
			boolean hapticFeedback) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.canEdit = canEdit;
		this.screenBrightness = screenBrightness;
		this.screenTimeout = screenTimeout;
		this.vibrate = vibrate;
		this.wifi = wifi;
		this.bluetooth = bluetooth;
		this.mobileData = mobileData;
		this.sync = sync;
		this.hapticFeedback = hapticFeedback;
	}
	
	@Override
	public boolean equals(Object o) {
        if (!(o instanceof OptimalMode)) return false;
        final OptimalMode other = (OptimalMode) o;
        return id == other.id;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

}
