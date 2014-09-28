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

import vn.cybersoft.obs.android.provider.PowerSchedule;
import vn.cybersoft.obs.android.utilities.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class BatteryChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Log.LOGV) {
			Log.v("******BatteryChangeReceiver.onReceive()");
		}
		
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		int percent = level * 100 / scale;
		
		String selection = PowerSchedule.BATTERY_LEVEL + "=?";
		String[] selectionArgs = { Integer.toString(percent) }; 
		List<PowerSchedule> schedules = PowerSchedule.getSchedules(context.getContentResolver(), selection , selectionArgs);
		
		//we get the first schedule if get one more schedule.
		if (schedules.size() > 0) {
			if (Log.LOGV) {
				Log.v("******BatteryChangeReceiver.onReceive(): startExecuteService");
			}
			Intent i = new Intent(PowerSchedule.EXECUTE_SCHEDULE_ACTION);
			i.putExtra(PowerSchedule.INTENT_EXTRA, schedules.get(0));
			context.startService(i);
		}
		
	}

}
