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
import vn.cybersoft.obs.android.preference.ModePreference;
import vn.cybersoft.obs.android.preference.PowerPecentPreference;
import vn.cybersoft.obs.android.provider.PowerSchedule;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.ToastManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class SetPowerScheduleActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	public static final String KEY_ENABLED = "power_shedule_enabled";
	public static final String KEY_BATTERY_LEVEL = "power_schedule_level";
	public static final String KEY_MODE = "power_schedule_mode";
	
	private CheckBoxPreference mEnabledPref;
	private PowerPecentPreference mBatteryLevelPref;
	private ModePreference mModePref;
	
	private PowerSchedule mOriginalSchedule;
	
	private long mId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Override the default content view.
		setContentView(R.layout.set_schedule_layout);

		addPreferencesFromResource(R.xml.power_schedule_prefs);
		
		mEnabledPref = (CheckBoxPreference) findPreference(KEY_ENABLED);
		mBatteryLevelPref = (PowerPecentPreference) findPreference(KEY_BATTERY_LEVEL);
		mBatteryLevelPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(getString(R.string.percentage, Integer.valueOf((String) newValue))); 
				return true;
			}
		});
		mModePref = (ModePreference) findPreference(KEY_MODE);
		mModePref.setOnPreferenceChangeListener(this); 
		
        Intent i = getIntent();
        mId = i.getLongExtra(PowerSchedule.EXTRA_ID, -1);
        if (Log.LOGV) {
            Log.v("In SetPowerScheduleActivity, id = " + mId);
        }
        
        PowerSchedule schedule = null;
        if (mId == -1) {
            // No schedule id means create a new schedule.
        	schedule = new PowerSchedule();
			setTitle(getString(R.string.app_name) + 
					" > " + getString(R.string.add_new_power_schedule));
        } else {
            /* load schedule details from database */
        	schedule = PowerSchedule.getSchedule(getContentResolver(), mId);
        	setTitle(getString(R.string.app_name) + 
					" > " + getString(R.string.edit_power_schedule));
            // Bad schedule
            if (schedule == null) {
                finish();
                return;
            }
        }
        
        mOriginalSchedule = schedule;
        updatePrefs(mOriginalSchedule);
        
        getListView().setItemsCanFocus(true);
        
		// Attach actions to each button.
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isFillMode = mModePref.getModeId() != -1;
				boolean isFillBatLevel = !TextUtils.isEmpty(mBatteryLevelPref.getText());
				if (isFillMode && isFillBatLevel) {
					saveSchedule();
					finish();
				} else {
					int msgResId = R.string.save_schedule_unknow_err;
					if (!isFillBatLevel) {
						msgResId = R.string.save_schedule_power_empty_err;
					} else if (!isFillMode) {
						msgResId = R.string.save_schedule_mode_empty_err;
					}
					createToast(SetPowerScheduleActivity.this, getString(msgResId));
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
	
	private void updatePrefs(PowerSchedule schedule) {
		mId = schedule.id;
		mEnabledPref.setChecked(schedule.enabled);
		if (mId != -1) {
			mBatteryLevelPref.setText(Integer.toString(schedule.level));
			mBatteryLevelPref.setSummary(getString(R.string.percentage, schedule.level));
		}
		mModePref.setModeId(schedule.modeId); 
	}
	
    private void saveSchedule() {
    	PowerSchedule schedule = new PowerSchedule();
    	schedule.id = mId;
    	schedule.enabled = mEnabledPref.isChecked();
    	schedule.level = Integer.valueOf(mBatteryLevelPref.getText());
    	schedule.modeId = mModePref.getModeId();
    	
    	Log.i("In SetPowerSchedule ModeId: " + schedule.modeId); 
    	
    	if (schedule.id == -1) {
    		PowerSchedule.addSchedule(getContentResolver(), schedule);
    		
			mId = schedule.id;
		} else {
			PowerSchedule.updateSchedule(getContentResolver(), schedule);
		}
    }
    
    static void createToast(Context context, String message) {
    	Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	ToastManager.setToast(toast);
    	toast.show();
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
		String entry = (String) ((ListPreference) preference).getEntries()[index];
		((ListPreference) preference).setSummary(entry);
		return true;
	}
}
