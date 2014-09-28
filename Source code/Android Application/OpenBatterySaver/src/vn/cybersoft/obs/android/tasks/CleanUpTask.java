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

import java.util.HashMap;
import java.util.List;

import vn.cybersoft.obs.android.listeners.CleanUpListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class CleanUpTask extends AsyncTask<Void, String, HashMap<String, String>> { 
	private Context mContext;
	private CleanUpListener mListener;
	private HashMap<String, String> mResults;
	
	public CleanUpTask(Context context) {
		mContext = context;
		mResults = new HashMap<String, String>();
	}

	@Override
	protected HashMap<String, String> doInBackground(Void... params) {
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager packageManager = mContext.getPackageManager();
		List<RunningTaskInfo> list = activityManager.getRunningTasks(Integer.MAX_VALUE);

		for (RunningTaskInfo runningTaskInfo : list) {
			String packageName = runningTaskInfo.baseActivity.getPackageName();
			// If an Application is a non-system application it must have a launch Intent
			// by which it can be launched. If the launch intent is null then its a system App.	
			if (packageManager.getLaunchIntentForPackage(packageName) == null ||
				packageManager.equals(mContext.getPackageName())) {
				continue;
			}
			activityManager.killBackgroundProcesses(packageName);
		}
		return mResults;
	}
	
	@Override
	protected void onPostExecute(HashMap<String, String> results) {
		synchronized (this) {
			if (mListener != null) {
				mListener.cleaningComplete(results); 
			}
		}
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}
	
	public void setCleanerListener(CleanUpListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

}
