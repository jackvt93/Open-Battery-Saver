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

package vn.cybersoft.obs.android.activities;

import vn.cybersoft.obs.android.R;
import android.R.anim;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

/**
 * @author Atom
 *
 */
public class SwitchModeConfirmActivity extends SherlockActivity {
	public static final int LAYOUT_ID = R.layout.switch_mode_confirm_layout;
	public static final String KEY_EXTRA_TITLE = "title";
	public static final String KEY_EXTRA_BRIGHTNESS = "screen_brightness";
	public static final String KEY_EXTRA_SCREEN_TIMEOUT = "screen_timeout";
	public static final String KEY_EXTRA_VIBRATE = "vibrate";
	public static final String KEY_EXTRA_WIFI = "wifi";
	public static final String KEY_EXTRA_BLUETOOTH = "bluetooth";
	public static final String KEY_EXTRA_SYNC = "sync";
	public static final String KEY_EXTRA_HAPTIC = "haptic_feeback";
	public static final String KEY_EXTRA_POSITIVE_TEXT = "positive_button_text";
	public static final String KEY_EXTRA_NEGATIVE_TEXT = "negative_button_text";
	
	private TextView mTitleText, mScreenBrightnessText, 
	mScreenTimeoutText, mVibrateText, mWifiText, mBluetoothText, mSyncText, mHapticFeedbackText;
	
	private Button mPositiveButton, mNegativeButton;
	String positiveButtonText, negativeButtonText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showAsPopup();
		setContentView(LAYOUT_ID);
		
		ActionBar actionBar = getSupportActionBar();
		
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.message_actionbar_layout); 
		
		// find views
		mTitleText = (TextView) actionBar.getCustomView().findViewById(R.id.text1);
		mScreenBrightnessText = (TextView) findViewById(R.id.screenBrightnessText);
		mScreenTimeoutText = (TextView) findViewById(R.id.screenTimeoutText);
		mVibrateText = (TextView) findViewById(R.id.vibrateText);
		mWifiText = (TextView) findViewById(R.id.wifiText);
		mBluetoothText = (TextView) findViewById(R.id.bluetoothText);
		mSyncText = (TextView) findViewById(R.id.syncText);
		mHapticFeedbackText = (TextView) findViewById(R.id.hapticFeedbackText);
		
		mPositiveButton = (Button) findViewById(R.id.positiveButton);
		mNegativeButton = (Button) findViewById(R.id.negativeButton);
		
		// get extras
		Bundle extras = getIntent().getExtras();
		
		if(extras == null) {
			return;
		}
		
		// set data to viewss
		mTitleText.setText(extras.containsKey(KEY_EXTRA_TITLE) ? extras.getString(KEY_EXTRA_TITLE) : ""); 
		mScreenBrightnessText.setText(extras.containsKey(KEY_EXTRA_BRIGHTNESS) ?
				extras.getString(KEY_EXTRA_BRIGHTNESS) : getString(R.string.none));
		mScreenTimeoutText.setText(extras.containsKey(KEY_EXTRA_SCREEN_TIMEOUT) ?
				extras.getString(KEY_EXTRA_SCREEN_TIMEOUT) : getString(R.string.none));
		mVibrateText.setText(extras.containsKey(KEY_EXTRA_VIBRATE) ?
				extras.getString(KEY_EXTRA_VIBRATE) : getString(R.string.none));
		mWifiText.setText(extras.containsKey(KEY_EXTRA_WIFI) ?
				extras.getString(KEY_EXTRA_WIFI) : getString(R.string.none));
		mBluetoothText.setText(extras.containsKey(KEY_EXTRA_BLUETOOTH) ?
				extras.getString(KEY_EXTRA_BLUETOOTH) : getString(R.string.none));
		mSyncText.setText(extras.containsKey(KEY_EXTRA_SYNC) ?
				extras.getString(KEY_EXTRA_SYNC) : getString(R.string.none));
		mHapticFeedbackText.setText(extras.containsKey(KEY_EXTRA_HAPTIC) ?
				extras.getString(KEY_EXTRA_HAPTIC) : getString(R.string.none));
		
		mPositiveButton.setText(extras.containsKey(KEY_EXTRA_POSITIVE_TEXT) ? 
				extras.getString(KEY_EXTRA_POSITIVE_TEXT) : getString(android.R.string.ok));
		mPositiveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		mNegativeButton.setText(extras.containsKey(KEY_EXTRA_NEGATIVE_TEXT) ? 
				extras.getString(KEY_EXTRA_NEGATIVE_TEXT) : getString(android.R.string.cancel));
		mNegativeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	private void showAsPopup() {
		this.requestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = this.getWindow().getAttributes(); 
	    params.height = LayoutParams.WRAP_CONTENT; 
	    params.width = 850; 
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    this.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
	}
}

