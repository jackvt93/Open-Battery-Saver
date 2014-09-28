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
import java.text.DateFormatSymbols;
import java.util.Calendar;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.provider.OptimalMode;
import vn.cybersoft.obs.android.provider.TimeSchedule;
import vn.cybersoft.obs.android.utilities.Utils;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Luan Vu (hlcu.cybersoft@gmail.com)
 *
 */
// extends FragmentActivity to use with cursorloader in android device above api 11
public class TimeScheduleActivity extends FragmentActivity 
			implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final String t = TimeScheduleActivity.class.getSimpleName();
	
	public static final int LAYOUT_ID = R.layout.schedule_layout;
	static final String PREFERENCES = "ScheduleMode";
	
	private final static String M12 = "h:mm";
	
	private static final int TIME_SCHEDULE_LIST_LOADER = 0x01;
	
    //private SharedPreferences mPrefs;
    private LayoutInflater mInflater;
    private ListView mScheduleList;
    private ScheduleAdapter mSchedules;
    //private Cursor mCursor;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInflater = LayoutInflater.from(this);
        //mPrefs = getSharedPreferences(PREFERENCES, 0);
        //mCursor = TimeSchedule.getTimeSchedulesCursor(getContentResolver());
        updateLayout();
	}
	
    private void updateLayout() {
        setContentView(LAYOUT_ID);
        
		setTitle(getString(R.string.app_name) + 
				" > " + getString(R.string.schedule_by_time));
		
        mScheduleList = (ListView) findViewById(android.R.id.list);
        //mSchedules = new ScheduleAdapter(this, R.layout.schedule_list_row, mCursor);
        mSchedules = new ScheduleAdapter(this, R.layout.time_schedule_list_row);
        mScheduleList.setAdapter(mSchedules);
        mScheduleList.setVerticalScrollBarEnabled(true);
        mScheduleList.setOnItemClickListener(this);
        mScheduleList.setOnCreateContextMenuListener(this);
        
        getSupportLoaderManager().initLoader(TIME_SCHEDULE_LIST_LOADER, null, this);

        View addSchedule = findViewById(R.id.add_schedule);
        addSchedule.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    addNewSchedule();
                }
            });
        // Make the entire view selected when focused.
        addSchedule.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setSelected(hasFocus);
                }
        });

    }
    
    private void addNewSchedule() {
        startActivity(new Intent(this, SetTimeScheduleActivity.class));
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
    		ContextMenuInfo menuInfo) {
        // Inflate the menu from xml.
        getMenuInflater().inflate(R.menu.time_schedule_context_menu, menu);

        // Use the current item to create a custom view for the header.
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final Cursor c =
                (Cursor) mScheduleList.getAdapter().getItem((int) info.position);
        final TimeSchedule schedule = new TimeSchedule(c);

        // Construct the Calendar to compute the time.
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, schedule.hour);
        cal.set(Calendar.MINUTE, schedule.minutes);
        final String time = TimeSchedule.formatTime(this, cal);

        // Inflate the custom view and set each TextView's text.
        final View v = mInflater.inflate(R.layout.context_menu_header, null);
        TextView textView = (TextView) v.findViewById(R.id.header_time);
        textView.setText(time);
        textView = (TextView) v.findViewById(R.id.header_mode);
        
        textView.setText(Utils.getString(this, OptimalMode.getMode(getContentResolver(), schedule.modeId).name, R.string.class));  

        // Set the custom view on the menu.
        menu.setHeaderView(v);
        // Change the text based on the state of the schedule.
        if (schedule.enabled) {
            menu.findItem(R.id.enable_schedule).setTitle(R.string.disable_schedule);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final long id = info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_schedule:
                // Confirm that the schedule will be deleted.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_schedule))
                        .setMessage(getString(R.string.delete_schedule_confirm))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int w) {
                                    	TimeSchedule.deleteTimeSchedule(TimeScheduleActivity.this, id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.enable_schedule:
                final Cursor c = (Cursor) mScheduleList.getAdapter().getItem(info.position);
                final TimeSchedule schedule = new TimeSchedule(c);
                TimeSchedule.enableTimeSchedule(this, schedule.id, !schedule.enabled);
                if (!schedule.enabled) {
                	//TODO
                }
                return true;

            case R.id.edit_schedule:
                Intent intent = new Intent(this, SetTimeScheduleActivity.class);
                intent.putExtra(TimeSchedule.EXTRA_ID, id);
                startActivity(intent);
                return true;

            default:
                break;
        }
    	return super.onContextItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, SetTimeScheduleActivity.class);
        intent.putExtra(TimeSchedule.EXTRA_ID, id);
        startActivity(intent);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    // This is called when a new Loader needs to be created. This
	    // sample only has one Loader, so we don't care about the ID.
	    // First, pick the base URI to use depending on whether we are
	    // currently filtering.
	    return TimeSchedule.getSchedulesCursorLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
	    // Swap the new cursor in. (The framework will take care of closing the
	    // old cursor once we return.)
		mSchedules.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	    // This is called when the last Cursor provided to onLoadFinished()
	    // above is about to be closed. We need to make sure we are no
	    // longer using it.
		mSchedules.swapCursor(null);
	}
	
    private class ScheduleAdapter extends ResourceCursorAdapter {

		public ScheduleAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, 0);
		}
		
		public ScheduleAdapter(Context context, int layout) {
			super(context, layout, null, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final TimeSchedule schedule = new TimeSchedule(cursor);
			
			ToggleButton scheduleOnOff = 
					(ToggleButton) view.findViewById(R.id.schedule_onOff);
			scheduleOnOff.setChecked(schedule.enabled);
			
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, schedule.hour);
            c.set(Calendar.MINUTE, schedule.minutes);

            TextView timeDisplay = (TextView) view.findViewById(R.id.timeDisplay);
            TextView amPm = (TextView) view.findViewById(R.id.am_pm);
            
            // format date time
            // if date time is 12M show am_pm text view
            String format = TimeSchedule.get24HourMode(TimeScheduleActivity.this) ? TimeSchedule.M24 : M12;
            CharSequence time = DateFormat.format(format, c);
            
            timeDisplay.setText(time); 
            String[] ampm = new DateFormatSymbols().getAmPmStrings();
            
            amPm.setVisibility(format == M12 ? View.VISIBLE : View.GONE);
            // if is morning, text = "AM", if not is "PM" 
            amPm.setText(c.get(Calendar.AM_PM) == 0 ? ampm[0] : ampm[1]);
            
            timeDisplay.setTypeface(Typeface.DEFAULT);
            
			TextView modeToChange = (TextView) view.findViewById(R.id.text2);
			final String modeNameStr = Utils.getString(mContext, OptimalMode.getMode(getContentResolver(), schedule.modeId).name, R.string.class);
			modeToChange.setText(getString(R.string.mode_to_change, modeNameStr)); 
			
            TextView daysOfWeekView = (TextView) view.findViewById(R.id.text3);
            final String daysOfWeekStr = schedule.daysOfWeek.toString(TimeScheduleActivity.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText("(" + daysOfWeekStr + ")");
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }
		}
    }

}
