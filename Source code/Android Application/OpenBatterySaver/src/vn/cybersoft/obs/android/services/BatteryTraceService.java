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

import vn.cybersoft.obs.android.provider.BatteryTrace;
import vn.cybersoft.obs.android.utilities.DeviceUtils;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class BatteryTraceService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		
		BatteryTrace batteryTrace = new BatteryTrace();
		batteryTrace.hour = c.get(Calendar.HOUR_OF_DAY); 
		batteryTrace.minutes = c.get(Calendar.MINUTE);
		batteryTrace.level = DeviceUtils.getCurrentBatteryLevel(getApplicationContext()); 
		batteryTrace.date = c.getTime();
		
		BatteryTrace.add(getContentResolver(), batteryTrace);
		
		return START_STICKY;
	}

}
