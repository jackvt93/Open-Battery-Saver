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
package vn.cybersoft.obs.android.receivers;

import java.util.List;

import vn.cybersoft.obs.android.fragments.SmartTabFragment;
import vn.cybersoft.obs.android.utilities.Connectivity;
import vn.cybersoft.obs.android.utilities.DeviceUtils;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.Utils;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class ScreenStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		boolean isNetworkControlEnabled = (Boolean) Utils.getValueFromPreference(context, Boolean.class, SmartTabFragment.KEY_NETWORK_CONTROL, true);
		if (isNetworkControlEnabled) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if (!Connectivity.isConnected(context)) {
					return;
				}
				
				if (Connectivity.isConnectedWifi(context)) { 
					DeviceUtils.turnOnWifi(context, false);
					Utils.saveToPreference(context, "lastConnectedType", ConnectivityManager.TYPE_WIFI);
				}
				
				if (Connectivity.isConnectedMobile(context)) {
					DeviceUtils.turnOnDataConnection(context, false);
					Utils.saveToPreference(context, "lastConnectedType", ConnectivityManager.TYPE_MOBILE);
				} 
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				int lastConnectedType = (Integer) Utils.getValueFromPreference(context, Integer.class, "lastConnectedType", -1);
				
				if (lastConnectedType == -1) {
					return;
				}
				
				if (lastConnectedType == ConnectivityManager.TYPE_WIFI) {
					DeviceUtils.turnOnWifi(context, true);
				} else if (lastConnectedType == ConnectivityManager.TYPE_MOBILE) { 
					DeviceUtils.turnOnDataConnection(context, true); 
				}
				Utils.removeValueFromPreference(context, "lastConnectedType");
			}
		}
		
		boolean isCLearAppsScreenLock = (Boolean) Utils.getValueFromPreference(context, Boolean.class, SmartTabFragment.KEY_CLEAR_APP_SCREEN_LOCK, true);
		if (isCLearAppsScreenLock) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { 
				if (Log.LOGV) {
					Log.v("******* Clear apps when screen lock enabled");
				}
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
						PackageManager packageManager = context.getPackageManager();
						List<RunningTaskInfo> list = activityManager.getRunningTasks(Integer.MAX_VALUE);

						for (RunningTaskInfo runningTaskInfo : list) {
							String packageName = runningTaskInfo.baseActivity.getPackageName();
							// If an Application is a non-system application it must have a launch Intent
							// by which it can be launched. If the launch intent is null then its a system App.	
							if (packageManager.getLaunchIntentForPackage(packageName) == null) {
								continue;
							}
							activityManager.killBackgroundProcesses(packageName);
							if (Log.LOGV) {
								Log.v("******* killBackgroundProcesses: " + packageName); 
							}
						}
						return null;
					}
				}.execute(); 
			}
		}
	}
}
