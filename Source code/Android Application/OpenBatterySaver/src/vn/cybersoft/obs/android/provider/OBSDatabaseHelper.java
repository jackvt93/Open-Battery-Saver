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
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class OBSDatabaseHelper extends SQLiteOpenHelper {
	/**
     * Original OBS Database.
     **/
	private static final int VERSION_1 = 1;
	
    // This creates a default original mode name "short"
    private static final String DEFAULT_MODE_1 = "('mode_name_short', 'mode_desc_short', 0, 255, 600000, 1, 1, 1, 1, 1);";

    // This creates a default original mode name "long"
    private static final String DEFAULT_MODE_2 = "('mode_name_long', 'mode_desc_long', 0, 25, 15000, 0, 0, 0, 0, 0);";
	
    // Database and table names
    static final String DATABASE_NAME = "obs.db";
    static final String TIME_SCHEDULES_TABLE_NAME = "time_schedules";
    static final String OPTIMAL_MODES_TABLE_NAME = "optimal_modes";
    
    private static void createTimeSchedulesTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TIME_SCHEDULES_TABLE_NAME + " (" +
				DataProviderApi.TimeSchedulesColumns._ID + " INTEGER PRIMARY KEY, " +
				DataProviderApi.TimeSchedulesColumns.HOUR + " INTEGER NOT NULL, " +
				DataProviderApi.TimeSchedulesColumns.MINUTES + " INTEGER NOT NULL, " +
				DataProviderApi.TimeSchedulesColumns.DAYS_OF_WEEK + " INTEGER NOT NULL, " +
				DataProviderApi.TimeSchedulesColumns.SCHEDULE_TIME + " INTEGER NOT NULL, " +
				DataProviderApi.TimeSchedulesColumns.ENABLED + " INTEGER NOT NULL, " +
				DataProviderApi.TimeSchedulesColumns.MODE_ID + " INTEGER REFERENCES " +
					OPTIMAL_MODES_TABLE_NAME + "(" + DataProviderApi.OptimalModesColumns._ID + ") " +
					"ON UPDATE CASCADE ON DELETE CASCADE" + ");");
		Log.i("Schedules table created");
    }
    
    private static void createOptimalModesTable(SQLiteDatabase db) {
    	db.execSQL("CREATE TABLE " + OPTIMAL_MODES_TABLE_NAME + " (" +
    			DataProviderApi.OptimalModesColumns._ID + " INTEGER PRIMARY KEY, " +
    			DataProviderApi.OptimalModesColumns.NAME + " TEXT NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.DESC + " TEXT, " + 
    			DataProviderApi.OptimalModesColumns.CAN_EDIT + " INTEGER NOT NULL, " +
    			DataProviderApi.OptimalModesColumns.SCREEN_BRIGHTNESS + " INTEGER NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.SCREEN_TIMEOUT + " INTEGER NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.VIBRATE + " INTEGER NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.WIFI + " INTEGER NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.BLUETOOTH + " INTEGER NOT NULL, " +
    			DataProviderApi.OptimalModesColumns.SYNC + " INTEGER NOT NULL, " + 
    			DataProviderApi.OptimalModesColumns.HAPTIC_FEEDBACK + " INTEGER NOT NULL" + ");");
    	Log.i("Modes table created");
    }
    
    private Context mContext;
    
	public OBSDatabaseHelper(Context context) {
		super(context, Environment.getExternalStorageDirectory() + File.separator + DATABASE_NAME, null, VERSION_1);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTimeSchedulesTable(db);
		createOptimalModesTable(db);
		
        // insert default modes
        Log.i("Inserting default optimal modes");
        String cs = ", "; //comma and space
        String insertMe = "INSERT INTO " + OPTIMAL_MODES_TABLE_NAME + " (" +
                DataProviderApi.OptimalModesColumns.NAME + cs +
                DataProviderApi.OptimalModesColumns.DESC + cs +
                DataProviderApi.OptimalModesColumns.CAN_EDIT + cs +
                DataProviderApi.OptimalModesColumns.SCREEN_BRIGHTNESS + cs +
                DataProviderApi.OptimalModesColumns.SCREEN_TIMEOUT + cs +
                DataProviderApi.OptimalModesColumns.VIBRATE + cs +
                DataProviderApi.OptimalModesColumns.WIFI + cs +
                DataProviderApi.OptimalModesColumns.BLUETOOTH + cs +
                DataProviderApi.OptimalModesColumns.SYNC + cs +
                DataProviderApi.OptimalModesColumns.HAPTIC_FEEDBACK + ") VALUES ";
        db.execSQL(insertMe + DEFAULT_MODE_1);
        db.execSQL(insertMe + DEFAULT_MODE_2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Log.LOGV) {
            Log.v("Upgrading alarms database from version " + oldVersion + " to " + newVersion);
        }
	}

}
