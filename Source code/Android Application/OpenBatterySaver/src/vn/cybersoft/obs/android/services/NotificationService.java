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

import java.util.Calendar;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.activities.MainActivity;
import vn.cybersoft.obs.android.receivers.ScreenStateReceiver;
import vn.cybersoft.obs.android.utilities.DisplayUtils;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.Utils;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

public class NotificationService extends Service {
	public static final int LAYOUT_ID = R.layout.notification_layout;
	
	public static final int ONGOING_NOTIFICATION_ID = 0xABCDE;
	
	private ScreenStateReceiver mScreenStateReceiver = null; 
	
	private AlarmManager mAlarmManager;
	private PendingIntent mBatteryTraceAction;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent appIntent = new Intent(this, MainActivity.class); 
		appIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent startAppPintent = PendingIntent.getActivity(this, 0, appIntent, 0);
		
		RemoteViews contentViews = new RemoteViews(getPackageName(), LAYOUT_ID);
		contentViews.setOnClickPendingIntent(R.id.app_icon, startAppPintent);
		regScreenStateReceiver();
		
		startTraceBattery();
		
		Notification notification = DisplayUtils.createNotification(contentViews, R.drawable.ic_launcher);
		startForeground(ONGOING_NOTIFICATION_ID, notification);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mScreenStateReceiver);
		if (mAlarmManager != null) {
			if (Log.LOGV) {
				Log.v("******* cancel BatteryTrace Alarm"); 
			}
			mAlarmManager.cancel(mBatteryTraceAction); 
		}
		super.onDestroy();
	}
	
	private void regScreenStateReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		mScreenStateReceiver = new ScreenStateReceiver();
		registerReceiver(mScreenStateReceiver, intentFilter);
	}
	
	private void startTraceBattery() {
		Intent intent = new Intent("vn.cybersoft.obs.android.intent.action.BATTERY_TRACE");
		mBatteryTraceAction = PendingIntent.getBroadcast(
				getApplicationContext(), 0, intent, 0);

		// Set alarm to start at 6:00 am, and repeat every 1 hour 30 minutes thereafter:
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.set(Calendar.MINUTE, 0);

		mAlarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		if (Utils.isKitKatOrLater()) {
			mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_HOUR + AlarmManager.INTERVAL_HALF_HOUR, mBatteryTraceAction);
		} else {
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_HOUR + AlarmManager.INTERVAL_HALF_HOUR, mBatteryTraceAction); 
		}
	}
	
}
