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
package vn.cybersoft.obs.android.services;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.activities.MainActivity;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.fragments.SmartTabFragment;
import vn.cybersoft.obs.android.receivers.ScreenStateReceiver;
import vn.cybersoft.obs.android.utilities.DisplayUtils;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

public class BatteryStatusService extends Service {
	public static final int LAYOUT_ID = R.layout.notification_layout;
	
	public static final int ONGOING_NOTIFICATION_ID = 0xABCDE;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent appIntent = new Intent(this, MainActivity.class); 
		appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent startAppPintent = PendingIntent.getActivity(this, 0, appIntent, 0);
		
		RemoteViews contentViews = new RemoteViews(getPackageName(), LAYOUT_ID);
		contentViews.setOnClickPendingIntent(R.id.app_icon, startAppPintent);
		
		registryOptimizationReceiver();
		
		Notification notification = DisplayUtils.createNotification(contentViews, R.drawable.ic_launcher);
		startForeground(ONGOING_NOTIFICATION_ID, notification);
		return START_STICKY;
	}
	
	private void registryOptimizationReceiver() {
		// Auto clear apps when screen clock receiver
		boolean autoClearAppScreenOff = OBS.getInstance().getSharePreferences().getBoolean(SmartTabFragment.KEY_AUTO_CLEAR_APP_SCREEN_LOCK, true);
		if(autoClearAppScreenOff) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
			intentFilter.addAction(Intent.ACTION_SCREEN_ON);
			BroadcastReceiver receiver = new ScreenStateReceiver();
			registerReceiver(receiver, intentFilter); 
		}
	}

}
