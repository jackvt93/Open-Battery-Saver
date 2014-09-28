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

import java.util.Calendar;

import vn.cybersoft.obs.andriod.batterystats2.service.ICounterService;
import vn.cybersoft.obs.andriod.batterystats2.service.UMLoggerService;
import vn.cybersoft.obs.android.services.NotificationService;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.Utils;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;

/**
 * @author Luan Vu
 *
 */
public class OBS extends Application {
	private static OBS singleton = null;

	private ICounterService mCounterService;

	public static OBS getInstance() {
		return singleton;
	}

	public static long getSelectedOptimalModeId() {
		return (Long) Utils.getValueFromPreference(OBS.getInstance(), Long.class, "optimal_mode", Long.valueOf(-1)); 
	}

	public static void saveOptimalModeId(long id) {
		Utils.saveToPreference(OBS.getInstance(), "optimal_mode", id);
	}
	
	public ICounterService getCounterService() {
		return mCounterService;
	}

	public static boolean isFirstRun() {
		// get the package info object with version number
		PackageInfo packageInfo = null;
		try {
			packageInfo =
					getInstance().getApplicationContext().getPackageManager().getPackageInfo(getInstance().getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		boolean firstRun = (Boolean)  Utils.getValueFromPreference(getInstance(), Boolean.class, "firstRun", true);

		// if you've increased version code, then update the version number and set firstRun to true
		if ((Integer) Utils.getValueFromPreference(getInstance(), Integer.class, "lastVersion", 0) < packageInfo.versionCode) {
			Utils.saveToPreference(getInstance(), "lastVersion", Integer.valueOf(packageInfo.versionCode));
			firstRun = true;
		}

		return firstRun;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		startNotificationService();
		startPowerMonitorService();

		if (isFirstRun()) {
			
		}
	}
	
	public void startPowerMonitorService() {
		Intent i = new Intent(this, UMLoggerService.class);
		CounterServiceConnection serviceConnection = new CounterServiceConnection();
		bindService(i, serviceConnection, 0);
		if (mCounterService == null) {
			if (Log.LOGV) {
				Log.v("******* start UMLoggerService"); 
			}
			startService(i);
		}
	}
	
	private void startNotificationService() {
		Intent i = new Intent(this, NotificationService.class);
		startService(i);
	}

	private class CounterServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCounterService = ICounterService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
	}

}
