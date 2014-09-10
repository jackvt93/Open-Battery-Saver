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

import vn.cybersoft.obs.android.provider.TimeSchedule;
import vn.cybersoft.obs.android.utilities.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class TimeScheduleReceiver extends BroadcastReceiver {
    /** If the schedule is older than STALE_WINDOW, ignore.  It
    is probably the result of a time or timezone change */
	private final static int STALE_WINDOW = 30 * 60 * 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Time Schedule retrieve...");
		TimeSchedule schedule = null;
        // Grab the schedule from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // TimeSchedule object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(TimeSchedule.INTENT_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            schedule = TimeSchedule.CREATOR.createFromParcel(in);
        }

        if (schedule == null) {
            Log.wtf("Failed to parse the schedule from the intent");
            // Make sure we set the next action if needed.
            TimeSchedule.setNextAction(context);
            return;
        }

        // Disable this schedule if it does not repeat.
        if (!schedule.daysOfWeek.isRepeatSet()) {
        	TimeSchedule.enableTimeSchedule(context, schedule.id, false);
        } else {
            // Enable the next action if there is one. The above call to
            // enableTimeSchedule will call setNextAction so avoid calling it twice.
        	TimeSchedule.setNextAction(context);
        }

        long now = System.currentTimeMillis();
        Log.v("Recevied schedule set for " + Log.formatTime(schedule.time));

        // Always verbose to track down time change problems.
        if (now > schedule.time + STALE_WINDOW) {
            Log.v("Ignoring stale schedule");
            return;
        }

        // Run the schedule action
        Intent i = new Intent(TimeSchedule.SCHEDULE_MODE_ACTION);
        i.putExtra(TimeSchedule.INTENT_EXTRA, schedule);
        context.startService(i);
	}

}
