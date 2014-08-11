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

package vn.cybersoft.obs.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class ScreenStateReceiver extends BroadcastReceiver {
	public final static String t = ScreenStateReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// clear app
			Log.i(t, "device screen off");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
			Log.i(t, "device screen on");
		}
	}

}
