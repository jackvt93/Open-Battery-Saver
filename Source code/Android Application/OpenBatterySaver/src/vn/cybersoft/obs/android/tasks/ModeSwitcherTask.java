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
package vn.cybersoft.obs.android.tasks;

import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.provider.OptimalMode;
import vn.cybersoft.obs.android.utilities.DeviceUtils;
import vn.cybersoft.obs.android.utilities.Log;
import android.os.AsyncTask;

/**
 * @author Luan Vu
 *
 */
public class ModeSwitcherTask extends AsyncTask<Long, Void, String> {
	private static final String t = ModeSwitcherTask.class.getSimpleName();
	
	private ModeSwitcherListener listener = null;
	
	public void setModeSwitcherListener(ModeSwitcherListener listener) {
		synchronized (this) {
			this.listener = listener;
		}
	}

	@Override
	protected String doInBackground(Long... params) {
		long modeId = params[0];
		
		OptimalMode mode = OptimalMode.getMode(OBS.getInstance().getContentResolver(), modeId);
		
		if (null == mode) {
			Log.e(t + ".doInBackground(): " + "Bad query in mode table"); 
			return "Error: Can't found this optimal mode.";
		}
		DeviceUtils.switchToOptimalMode(mode); 
		OBS.saveOptimalModeId(modeId); 
		return "";
	}
	
	@Override
	protected void onPostExecute(String error) {
		super.onPostExecute(error);
		
		if(null != listener) {
			if (error != "") {
				listener.switchError(error);
			} else {
				listener.switchComplete();
			}
		}
	}

	
	

}
