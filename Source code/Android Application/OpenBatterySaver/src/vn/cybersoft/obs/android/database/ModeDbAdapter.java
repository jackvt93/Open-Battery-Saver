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
package vn.cybersoft.obs.android.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.utilities.ReflectionUtils;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * @author Luan Vu
 *
 */
public class ModeDbAdapter {
	private static final String t = "ModeDbAdapter"; 

	private static String DATABASE_PATH = Environment.getExternalStorageDirectory().toString();
	private static final String DATABASE_NAME = "data.db";
	private static final int DATABASE_VERSION = 1;
	private static final String OP_MODE_TABLE_NAME = "optimal_mode";
	
	//optimal mode table
	public static final String _ID = "_id";
	public static final String NAME = "name";
	public static final String DESC = "desc";
	public static final String CAN_EDIT = "canEdit";
	public static final String SCREEN_BRIGHTNESS = "screen_brightness";
	public static final String SCREEN_TIMEOUT = "screen_timeout";
	public static final String VIBRATE = "vibrate";
	public static final String WIFI = "wifi";
	public static final String BLUETOOTH = "bluetooth";
	public static final String SYNC = "sync";
	public static final String HAPTIC_FEEDBACK = "haptic_feedback";
	
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb; 
	private Context mContext;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context, String databaseName) throws IOException {
			super(context, databaseName, null, DATABASE_VERSION);
			createDataBase();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
		/**
		 * Copies your database from your local assets-folder to the just
		 * created empty database in the system folder, from where it can be
		 * accessed and handled. This is done by transfering bytestream.
		 * */
		public void copyDataBase() throws IOException {
			Log.i(t, "Copying database");

			/* Open your local db as the input stream */
			InputStream myInput = OBS.getInstance().getApplicationContext().getAssets().open(DATABASE_NAME);
			
			/* Path to the just created empty db */
			String outFileName = DATABASE_PATH + File.separator + DATABASE_NAME;
			File out = new File(outFileName);
			if (!out.exists())
				out.createNewFile();
			/* Open the empty db as the output stream */
			OutputStream myOutput = new FileOutputStream(outFileName);

			/* transfer bytes from the inputfile to the outputfile */
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) != -1) {
				myOutput.write(buffer, 0, length);
			}
			Log.i(t, "Database copied successfully");

			/* Close the streams */
			myOutput.flush();
			myOutput.close();
			myInput.close();

			Log.i(t, "DB exist: " + checkDataBase());

		}
		
		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase() {
			SQLiteDatabase checkDB = null;
			
			try {
				String myPath = DATABASE_PATH + File.separator + DATABASE_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
			} catch (SQLiteException e) {
				Log.i(t, "Database does't exist yet.");
			}

			if (checkDB != null) {
				checkDB.close();
			}

			return checkDB != null;
		}
		
		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * @throws IOException 
		 * */
		public void createDataBase() throws IOException{
			Log.i(t, "createDataBase");
			boolean dbExist = checkDataBase();
			Log.i(t, "dbExist : " + dbExist);

			/* By calling this method and empty database will be created
			 into the default system path
			 of your application so we are gonna be able to overwrite that
			 database with our database. */
			if(!dbExist) {
				this.getReadableDatabase();
				copyDataBase();
			} 
		}
	}
	
	/**
	 * 
	 */
	public ModeDbAdapter(Context context) {
		mDb = getDbHelper(context).getWritableDatabase();
		mContext = context;
	}
	
	private DatabaseHelper getDbHelper(Context context) {
		if (mDbHelper != null) {
			return mDbHelper;
		}
		try {
			mDbHelper = new DatabaseHelper(context, DATABASE_PATH + File.separator + DATABASE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mDbHelper;
	}
	
	public Cursor fetchAllMode() {
		return mDb.query(OP_MODE_TABLE_NAME, null, null, null, null, null, null);
	}
	
	public Cursor fetchMode(long id, String[] columns) {
		return mDb.query(OP_MODE_TABLE_NAME, columns, _ID + " = " + id, null, null, null, null);
	}
	
    public Cursor query(String selection, String[] selectionArgs) {
        return mDb.query(true, OP_MODE_TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
    }
    
    public String getModeNameStr(long id) {
    	String[] columns = new String[] { NAME, CAN_EDIT };
    	String ret = null;
    	
    	Cursor c = null;
    	try {
			c = fetchMode(id, columns);
	    	if (c.getCount() > 0) {
				c.moveToFirst();
				boolean canEdit = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.CAN_EDIT)));
	        	if (!canEdit) {
					int resId = ReflectionUtils.getResourceId(c.getString(c.getColumnIndex(ModeDbAdapter.NAME)), R.string.class);
					ret = resId != -1 ? mContext.getString(resId) : c.getString(c.getColumnIndex(ModeDbAdapter.NAME));
				} else {
					ret = c.getString(c.getColumnIndex(ModeDbAdapter.NAME));
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
    	return ret;
    }
	
}
