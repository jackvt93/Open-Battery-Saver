/*
 * Copyright (C) 2014 €yber$oft Team
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

package vn.cybersoft.obs.android.tasks;

import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.database.ModeDbAdapter;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.models.OptimalMode;
import vn.cybersoft.obs.android.utilities.DeviceUtils;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Luan Vu
 *
 */
public class ModeSwicherTask extends AsyncTask<Long, Void, String> {
	private static final String t = ModeSwicherTask.class.getSimpleName();
	
	private ModeSwitcherListener listener = null;
	
	public void setModeSwitcherListener(ModeSwitcherListener listener) {
		synchronized (this) {
			this.listener = listener;
		}
	}

	@Override
	protected String doInBackground(Long... params) {
		long modeId = params[0];
		Cursor c = null;
		
		try {
			c = OBS.getInstance().getModeDbAdapter().query(ModeDbAdapter._ID + " = " + modeId, null);
			c.moveToPosition(0);
			System.out.println(modeId); 
			
			String name = c.getString(c.getColumnIndex(ModeDbAdapter.NAME));
			int screenBrightness = c.getInt(c.getColumnIndex(ModeDbAdapter.SCREEN_BRIGHTNESS));
			int screenTimeout = c.getInt(c.getColumnIndex(ModeDbAdapter.SCREEN_TIMEOUT));
			boolean vibrate = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.VIBRATE)));
			boolean wifi = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.WIFI)));
			boolean bluetooth = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.BLUETOOTH)));
			boolean sync = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.SYNC)));
			boolean hapticFeed = Boolean.parseBoolean(c.getString(c.getColumnIndex(ModeDbAdapter.HAPTIC_FEEDBACK)));
			DeviceUtils.switchToOptimalMode(new OptimalMode(name, screenBrightness, screenTimeout, vibrate, wifi, bluetooth, sync, hapticFeed)); 
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		
		return "";
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Log.d(t, "Switch to optimal mode: " + result + " success !!");
		
		if(null != listener) {
			listener.switchComplete();
		}
	}

	
	

}
