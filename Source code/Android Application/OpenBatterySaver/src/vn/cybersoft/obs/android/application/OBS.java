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
package vn.cybersoft.obs.android.application;

import vn.cybersoft.obs.android.database.ModeDbAdapter;
import vn.cybersoft.obs.android.fragments.SmartTabFragment;
import vn.cybersoft.obs.android.receivers.ScreenStateReceiver;
import vn.cybersoft.obs.android.services.BatteryStatusService;
import vn.cybersoft.obs.android.utilities.Utils;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Luan Vu
 *
 */
public class OBS extends Application {
	private static OBS singleton = null;
	
	private ModeDbAdapter mModeDbAdapter;
	
	private SharedPreferences mPreferences;
	
	public static OBS getInstance() {
		return singleton;
	}
	
	public SharedPreferences getSharePreferences() {
		return mPreferences;
	}
	
	public static long getSelectedOptimalModeId() {
		return (Long) Utils.getValueFromPreference(OBS.getInstance(), Long.class, "optimal_mode", Long.valueOf(-1)); 
	}
	
	public static void saveOptimalModeId(long id) {
		Utils.saveToPreference(OBS.getInstance(), "optimal_mode", id);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		mModeDbAdapter = new ModeDbAdapter(getApplicationContext());
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
/*		// start application forground service
		Intent i = new Intent(this, BatteryStatusService.class);
		startService(i);*/
	}

	public ModeDbAdapter getModeDbAdapter() {
		return mModeDbAdapter;
	}
	
}
