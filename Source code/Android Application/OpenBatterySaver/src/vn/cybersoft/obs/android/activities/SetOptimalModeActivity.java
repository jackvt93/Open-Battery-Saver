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

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.provider.OptimalMode;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.ToastManager;
import vn.cybersoft.obs.android.utilities.Utils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class SetOptimalModeActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	
	public static final String KEY_SCREEN_BRIGHTNESS = "screen_brightness";
	public static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
	public static final String KEY_VIBRATE = "vibrate_on";
	public static final String KEY_WIFI = "wifi_on";
	public static final String KEY_BLUETOOTH = "bluetooth_on";
	public static final String KEY_MOBILE_DATA = "mobile_data_on";
	public static final String KEY_SYNC = "sync_on";
	public static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback_on";
	public static final String KEY_DESCRIPTION = "mode_description";
	
	
	private ListPreference mScreenBrightnessPreference;  
	private ListPreference mScreenTimeoutPreference; 
	private Preference mVibratePreference; 
	private Preference mWifiPreference; 
	private Preference mBluetoothPreference; 
	private Preference mMobileDataPreference; 
	private Preference mSyncPreference; 
	private Preference mHapticFeedbackPreference; 
	private EditTextPreference mDescriptionPreference;
	
	private TextView mInputName;
	
	private long mModeId;
	private OptimalMode mOriginalMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Override the default content view.
		setContentView(R.layout.set_optimal_mode_layout);
		
		addPreferencesFromResource(R.xml.optimal_mode_prefs);
		
		mScreenBrightnessPreference = (ListPreference) findPreference(KEY_SCREEN_BRIGHTNESS);
		mScreenBrightnessPreference.setOnPreferenceChangeListener(this);
		
		mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        
        mVibratePreference = findPreference(KEY_VIBRATE);
		mWifiPreference = findPreference(KEY_WIFI);
		mBluetoothPreference = findPreference(KEY_BLUETOOTH);
		mMobileDataPreference = findPreference(KEY_MOBILE_DATA);
		mSyncPreference = findPreference(KEY_SYNC);
		mHapticFeedbackPreference = findPreference(KEY_HAPTIC_FEEDBACK);
		
		mDescriptionPreference = (EditTextPreference) findPreference(KEY_DESCRIPTION);
		mDescriptionPreference
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference p,
							Object newValue) {
						String val = (String) newValue;
						p.setSummary(val);
						return true;
					}
				});
		
		mInputName = (TextView) findViewById(R.id.mode_name_input);
		
        Intent i = getIntent();
        mModeId = i.getLongExtra(OptimalMode.EXTRA_ID, -1);
        if (Log.LOGV) {
            Log.v("In SetOptimalModeActivity, mode id = " + mModeId);
        }
        
        OptimalMode mode = null;
        
        if (mModeId == -1) {
        	/* no mode found, create new mode */ 
			mode = new OptimalMode();
			setTitle(getString(R.string.app_name) + 
					" > " + getString(R.string.add_new_mode));
        } else {
			mode = OptimalMode.getMode(getContentResolver(), mModeId);
			setTitle(getString(R.string.app_name) + 
					" > " + getString(R.string.edit_mode));
	        /* bad case */
	        if (mode == null) {
	        	finish();
	        	return;
			}
		}
        
        mOriginalMode = mode;
        
        updatePrefs(mOriginalMode); 
        
        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
		getListView().setItemsCanFocus(true);
        
		// Attach actions to each button.
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(SetOptimalModeActivity.this.mInputName.getText())) {
					saveOptimalMode();
					finish();
				} else {
					createToast(SetOptimalModeActivity.this, getString(R.string.save_opt_mode_err)); 
				}
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ToastManager.cancelToast();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
    private void updatePrefs(OptimalMode mode) {
    	mInputName.setText(mode.name);
    	mScreenBrightnessPreference.setValue(String.valueOf(generalDefaultBrightnessValue(mode.screenBrightness)));
    	mScreenBrightnessPreference.setSummary(mScreenBrightnessPreference.getEntry()); 
    	
    	mScreenTimeoutPreference.setValue(String.valueOf(mode.screenTimeout));  
    	mScreenTimeoutPreference.setSummary(mScreenTimeoutPreference.getEntry());
    	//updateTimeoutPreferenceDescription(mode.screenBrightness); 
    	
    	if (Utils.isIceCreamSandwichOrLater()) {
			((TwoStatePreference) mVibratePreference).setChecked(mode.vibrate);
			((TwoStatePreference) mWifiPreference).setChecked(mode.vibrate);
			((TwoStatePreference) mBluetoothPreference).setChecked(mode.vibrate);
			((TwoStatePreference) mMobileDataPreference).setChecked(mode.mobileData);
			((TwoStatePreference) mSyncPreference).setChecked(mode.vibrate);
			((TwoStatePreference) mHapticFeedbackPreference).setChecked(mode.vibrate);
		} else {
			((CheckBoxPreference) mVibratePreference).setChecked(mode.vibrate);
			((CheckBoxPreference) mWifiPreference).setChecked(mode.vibrate);
			((CheckBoxPreference) mBluetoothPreference).setChecked(mode.vibrate);
			((CheckBoxPreference) mMobileDataPreference).setChecked(mode.mobileData);
			((CheckBoxPreference) mSyncPreference).setChecked(mode.vibrate);
			((CheckBoxPreference) mHapticFeedbackPreference).setChecked(mode.vibrate);
		}
    	
    	mDescriptionPreference.setText(mode.desc);
    	mDescriptionPreference.setSummary(mode.desc);
    	
    }
	
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
    private void saveOptimalMode() {
    	OptimalMode mode = new OptimalMode();
    	mode.id = mModeId;
    	mode.name = mInputName.getText().toString();
    	mode.screenBrightness = (Integer.valueOf(mScreenBrightnessPreference.getValue()) * 255) / 100;
    	mode.screenTimeout = Integer.valueOf(mScreenTimeoutPreference.getValue());
    	
    	if (Utils.isIceCreamSandwichOrLater()) {
			mode.vibrate = ((TwoStatePreference) mVibratePreference).isChecked();
			mode.wifi = ((TwoStatePreference) mWifiPreference).isChecked();
			mode.bluetooth = ((TwoStatePreference) mBluetoothPreference).isChecked();
			mode.mobileData = ((TwoStatePreference) mMobileDataPreference).isChecked();
			mode.sync = ((TwoStatePreference) mSyncPreference).isChecked();
			mode.hapticFeedback = ((TwoStatePreference) mHapticFeedbackPreference).isChecked();
		} else {
			mode.vibrate = ((CheckBoxPreference) mVibratePreference).isChecked();
			mode.wifi = ((CheckBoxPreference) mWifiPreference).isChecked();
			mode.bluetooth = ((CheckBoxPreference) mBluetoothPreference).isChecked();
			mode.mobileData = ((CheckBoxPreference) mMobileDataPreference).isChecked();
			mode.sync = ((CheckBoxPreference) mSyncPreference).isChecked();
			mode.hapticFeedback = ((CheckBoxPreference) mHapticFeedbackPreference).isChecked();
		}
    	
    	mode.desc = mDescriptionPreference.getText();
    	
    	if (mode.id == -1) {
    		OptimalMode.addMode(getContentResolver(), mode);
			mModeId = mode.id;
		} else {
			OptimalMode.updateMode(getContentResolver(), mode);
		}
    }
    
    static void createToast(Context context, String message) {
    	Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	ToastManager.setToast(toast);
    	toast.show();
    }
    
/*    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        CharSequence summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                summary = entries[best];
            }
        }
        preference.setSummary(summary);
    }*/
    
    private int generalDefaultBrightnessValue(int value) {
    	int round =   Math.round((float)value * 100 / 255);
    	float unRound = Math.round((float)round / 10);
    	int ret = (int) (unRound * 10);
    	return ret == 0 ? 10 : ret;
    }
    
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		//final String key = preference.getKey();
/*		if (KEY_SCREEN_TIMEOUT.equals(key)) {
			int value = Integer.parseInt((String) newValue);
			updateTimeoutPreferenceDescription(value);
		} else {*/
			int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
			String entry = (String) ((ListPreference) preference).getEntries()[index];
			((ListPreference) preference).setSummary(entry);
		//}
			
		return true;
	}
}
