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
import vn.cybersoft.obs.android.provider.PowerSchedule;
import vn.cybersoft.obs.android.utilities.Utils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class PowerScheduleActivity extends FragmentActivity implements OnItemClickListener, LoaderCallbacks<Cursor> {
	
	public static final int LAYOUT_ID = R.layout.schedule_layout;
	
	private static final int TIME_SCHEDULE_LIST_LOADER = 0x04;
	
	private LayoutInflater mInflater;
    private ListView mScheduleList;
    private ScheduleAdapter mSchedules;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT_ID); 
		
		setTitle(getString(R.string.app_name) + 
				" > " + getString(R.string.schedule_by_power));
		
		mInflater = LayoutInflater.from(this);
		getSupportLoaderManager().initLoader(TIME_SCHEDULE_LIST_LOADER, null, this);
		
        mScheduleList = (ListView) findViewById(android.R.id.list);
        mSchedules = new ScheduleAdapter(this, R.layout.power_schedule_list_row);
        mScheduleList.setAdapter(mSchedules);
        mScheduleList.setVerticalScrollBarEnabled(true);
        mScheduleList.setOnItemClickListener(this);
        mScheduleList.setOnCreateContextMenuListener(this);
        
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
        startActivity(new Intent(this, SetPowerScheduleActivity.class));
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
        final PowerSchedule schedule = new PowerSchedule(c);

        // Inflate the custom view and set each TextView's text.
        final View v = mInflater.inflate(R.layout.context_menu_header, null);
        TextView textView = (TextView) v.findViewById(R.id.header_time);
        textView.setText(getString(R.string.percentage, schedule.level)); 
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
                                    	PowerSchedule.deleteSchedule(getContentResolver(), id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.enable_schedule:
                final Cursor c = (Cursor) mScheduleList.getAdapter().getItem(info.position);
                final PowerSchedule schedule = new PowerSchedule(c);
                PowerSchedule.enableSchedule(getContentResolver(), schedule.id, !schedule.enabled);
                return true;

            case R.id.edit_schedule:
                Intent intent = new Intent(this, SetPowerScheduleActivity.class);
                intent.putExtra(PowerSchedule.EXTRA_ID, id);
                startActivity(intent);
                return true;

            default:
                break;
        }
    	return super.onContextItemSelected(item);
    }
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
        Intent intent = new Intent(this, SetPowerScheduleActivity.class);
        intent.putExtra(PowerSchedule.EXTRA_ID, id);
        startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return PowerSchedule.getCursorLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mSchedules.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
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
			final PowerSchedule schedule = new PowerSchedule(cursor);
			
			ToggleButton scheduleOnOff = 
					(ToggleButton) view.findViewById(R.id.schedule_onOff);
			scheduleOnOff.setChecked(schedule.enabled);
			
			TextView powerLevel = (TextView) view.findViewById(R.id.battery_level);
			powerLevel.setText(getString(R.string.percentage, schedule.level));
			
			TextView modeToChange = (TextView) view.findViewById(R.id.text2);
			final String modeNameStr = Utils.getString(mContext, OptimalMode.getMode(getContentResolver(), schedule.modeId).name, R.string.class);
			modeToChange.setText(getString(R.string.mode_to_change, modeNameStr)); 
			
		}
    }


}
