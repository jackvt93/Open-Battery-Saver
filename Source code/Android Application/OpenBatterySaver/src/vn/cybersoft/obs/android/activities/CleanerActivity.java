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

package vn.cybersoft.obs.android.activities;

import java.util.HashMap;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.listeners.CleanUpListener;
import vn.cybersoft.obs.android.tasks.CleanUpTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class CleanerActivity extends Activity implements CleanUpListener {
	private final static int PROGRESS_DIALOG = 1;
	
	private static final String ALERT_MSG = "alertmsg";
	private static final String ALERT_SHOWING = "alertshowing";
	
	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;

	private String mAlertMsg;
	private boolean mAlertShowing;
	
	private CleanUpTask mCleanUpTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		mAlertMsg = getString(R.string.please_wait);
		mAlertShowing = false;
		
		// get any simple saved state...
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(ALERT_MSG)) {
				mAlertMsg = savedInstanceState.getString(ALERT_MSG);
			}
			if (savedInstanceState.containsKey(ALERT_SHOWING)) {
				mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING, false);
			}
			setTitle(getString(R.string.app_name) + " > " + getString(R.string.clean_up));
		}
		
		mCleanUpTask = (CleanUpTask) getLastNonConfigurationInstance();
		if (mCleanUpTask == null) {
			showDialog(PROGRESS_DIALOG);
			mCleanUpTask = new CleanUpTask(getApplicationContext());
			mCleanUpTask.setCleanerListener(this);
			mCleanUpTask.execute();
		}
	}
	
	@Override
	protected void onResume() {
		if (mCleanUpTask != null) {
			mCleanUpTask.setCleanerListener(this);
		}
		if (mAlertShowing) {
			createAlertDialog(mAlertMsg);
		}
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ALERT_MSG, mAlertMsg);
		outState.putBoolean(ALERT_SHOWING, mAlertShowing);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return mCleanUpTask;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}	
	}
	
	@Override
	protected void onDestroy() {
		if (mCleanUpTask != null) {
			mCleanUpTask.setCleanerListener(null);
		}
		super.onDestroy();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			mProgressDialog = new ProgressDialog(this);
			DialogInterface.OnClickListener loadingButtonListener =
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mCleanUpTask.cancel(true);
					mCleanUpTask.setCleanerListener(null);
					finish();
				}
			};
			mProgressDialog.setTitle(getString(R.string.cleaning));
			mProgressDialog.setMessage(mAlertMsg);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(getString(android.R.string.cancel), loadingButtonListener);
			break;

		default:
			break;
		}
		
		return null;
	}
	
	@Override
	public void cleaningComplete(HashMap<String, String> result) {
		try {
			dismissDialog(PROGRESS_DIALOG);
		} catch (Exception e) {
			// tried to close a dialog not open. don't care.
		}
		
		createAlertDialog(getString(R.string.clean_done)); 
		
	}
	
	@Override
	public void progessUpdate(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	private void createAlertDialog(String message) {
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setTitle(getString(R.string.clean_up_result));
		mAlertDialog.setMessage(message);
		DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE: // ok
				// always exit this activity since it has no interface
				mAlertShowing = false;
				finish();
				break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(android.R.string.ok), quitListener);
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertShowing = true;
		mAlertMsg = message;
		mAlertDialog.show();
	}

}
