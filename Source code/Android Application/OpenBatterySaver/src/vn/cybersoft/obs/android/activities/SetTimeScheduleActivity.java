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
import vn.cybersoft.obs.android.preference.RepeatPreference;
import vn.cybersoft.obs.android.provider.TimeSchedule;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.ToastManager;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 * 
 */
public class SetTimeScheduleActivity extends PreferenceActivity implements TimePickerDialog.OnTimeSetListener, Preference.OnPreferenceChangeListener {
	public static final String KEY_ENABLED = "time_schedule_enabled";
	public static final String KEY_TIME = "time_schedule";
	public static final String KEY_REPEAT = "time_schedule_repeat";
	public static final String KEY_MODE = "time_schedule_mode";
	
	
	private CheckBoxPreference mEnabledPref;
	private Preference mTimePref;
	private RepeatPreference mRepeatPref;
	private ModePreference mModePref;

	private int mId;
	private int mHour;
	private int mMinutes;
	//private boolean mTimePickerCancelled;
	private TimeSchedule mOriginalSchedule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Override the default content view.
		setContentView(R.layout.set_time_schedule_layout);

		addPreferencesFromResource(R.xml.time_schedule_prefs);

		mEnabledPref = (CheckBoxPreference) findPreference(KEY_ENABLED);
		mEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference p, Object newValue) {
				return SetTimeScheduleActivity.this.onPreferenceChange(p, newValue);
			}
		});
		
		mTimePref = findPreference(KEY_TIME);
		mRepeatPref = (RepeatPreference) findPreference(KEY_REPEAT);
		mRepeatPref.setOnPreferenceChangeListener(this);
		mModePref = (ModePreference) findPreference(KEY_MODE);
		mModePref.setOnPreferenceChangeListener(this); 
		
        Intent i = getIntent();
        mId = i.getIntExtra(TimeSchedule.EXTRA_ID, -1);
        if (Log.LOGV) {
            Log.v("In SetTimeSchedule, schedule id = " + mId);
        }
		
        TimeSchedule schedule = null;
        if (mId == -1) {
            // No schedule id means create a new schedule.
        	schedule = new TimeSchedule();
        } else {
            /* load schedule details from database */
        	schedule = TimeSchedule.getTimeSchedule(getContentResolver(), mId);
            // Bad schedule
            if (schedule == null) {
                finish();
                return;
            }
        }
        
        mOriginalSchedule = schedule;
        
        updatePrefs(mOriginalSchedule);
        
        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
		getListView().setItemsCanFocus(true);
		
		// Attach actions to each button.
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mModePref.getModeId() != -1) {
					saveSchedule();
					finish();
				} else {
					createToast(SetTimeScheduleActivity.this, getString(R.string.save_schedule_err));
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
		
        // The last thing we do is pop the time picker if this is a new alarm.
        if (mId == -1) {
            showTimePicker();
        }
		
	}
	
    // Used to post runnables asynchronously.
    private static final Handler sHandler = new Handler();

	public boolean onPreferenceChange(final Preference p, Object newValue) {
		// Asynchronously save the schedule since this method is called _before_
		// the value of the preference has changed.
		sHandler.post(new Runnable() {
			public void run() {
				// Editing any preference (except enable) enables the schedule.
				if (p != mEnabledPref) {
					mEnabledPref.setChecked(true);
				}
			}
		});
		return true;
	}
	
    private void updatePrefs(TimeSchedule schedule) {
        mId = schedule.id;
        mEnabledPref.setChecked(schedule.enabled);
        mHour = schedule.hour;
        mMinutes = schedule.minutes;
        mRepeatPref.setDaysOfWeek(schedule.daysOfWeek);
        mModePref.setModeId(schedule.modeId); 
        updateTime();
    }
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
        if (preference == mTimePref) {
            showTimePicker();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
/*    @Override
    public void onBackPressed() {
        if (!mTimePickerCancelled) {
            saveSchedule();
        }
        finish();
    }*/

    private void showTimePicker() {
        new TimePickerDialog(this, this, mHour, mMinutes,
                DateFormat.is24HourFormat(this)).show();
    }

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinutes = minute;
        updateTime();
        // If the time has been changed, enable the schedule.
        mEnabledPref.setChecked(true);
	}
	
    private void updateTime() {
        if (Log.LOGV) {
            Log.v("updateTime " + mId);
        }
        mTimePref.setSummary(TimeSchedule.formatTime(this, mHour, mMinutes,
                mRepeatPref.getDaysOfWeek()));
    }
    
    private long saveSchedule() {
    	TimeSchedule schedule = new TimeSchedule();
    	schedule.id = mId;
    	schedule.enabled = mEnabledPref.isChecked();
    	schedule.hour = mHour;
    	schedule.minutes = mMinutes;
    	schedule.daysOfWeek = mRepeatPref.getDaysOfWeek();
    	schedule.modeId = mModePref.getModeId();
    	
    	Log.i("In SetTimeSchedule ModeId: " + schedule.modeId); 
    	
    	long time = 0;
    	if (schedule.id == -1) {
    		time = TimeSchedule.addTimeSchedule(this, schedule);
    		
			mId = schedule.id;
		} else {
			time = TimeSchedule.setTimeSchedule(this, schedule);
		}
    	return time;
    }
    
    private void deleteSchedule() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_schedule))
                .setMessage(getString(R.string.delete_schedule_confirm))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                TimeSchedule.deleteTimeSchedule(SetTimeScheduleActivity.this, mId);
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    static void createToast(Context context, String message) {
    	Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	ToastManager.setToast(toast);
    	toast.show();
    }
}
