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

import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.provider.TimeSchedule;
import vn.cybersoft.obs.android.tasks.ModeSwitcherTask;
import vn.cybersoft.obs.android.utilities.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * This class do actions of schedule when it goes off
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class TimeScheduleExecutorService extends Service implements ModeSwitcherListener {
	private TimeSchedule mCurrentSchedule;
	private ModeSwitcherTask mModeSwitcherTask;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        
        final TimeSchedule schedule = intent.getParcelableExtra(TimeSchedule.INTENT_EXTRA);
        
        if (schedule == null) {
            Log.v("TimeScheduleExcutorService failed to parse the schedule from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        if (mCurrentSchedule != null) {
        	
        }
        
        Toast.makeText(getApplicationContext(), "Schedule start ....", Toast.LENGTH_LONG).show();
        run(schedule); 
        mCurrentSchedule = schedule;
        
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void run(TimeSchedule schedule) {
        if (Log.LOGV) {
            Log.v("TimeScheduleExcutorService.run() " + schedule.id + " mode id: " + schedule.modeId);
        }
        if (mModeSwitcherTask != null) {
        	safeCleanSwitcherTask();
		}
		mModeSwitcherTask = new ModeSwitcherTask();
		mModeSwitcherTask.setModeSwitcherListener(this); 
		mModeSwitcherTask.execute(schedule.modeId);
	}

	@Override
	public void switchComplete() {
        if (Log.LOGV) {
            Log.v("TimeScheduleExcutorService.switchComplete()" );
        }
		safeCleanSwitcherTask();
	}

	@Override
	public void switchError(String errorMsg) {
        if (Log.LOGV) {
            Log.v("TimeScheduleExcutorService.switchError() " + errorMsg);
        }
		safeCleanSwitcherTask();
	}
	
	private void safeCleanSwitcherTask() {
		mModeSwitcherTask.setModeSwitcherListener(null);
		ModeSwitcherTask m = mModeSwitcherTask;
		mModeSwitcherTask = null;
		m.cancel(true);
	}


}
