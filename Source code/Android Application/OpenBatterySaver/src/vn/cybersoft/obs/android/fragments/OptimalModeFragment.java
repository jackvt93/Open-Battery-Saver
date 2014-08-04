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
package vn.cybersoft.obs.android.fragments;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.activities.SwitchModeConfirmActivity;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.database.ModeDbAdapter;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.tasks.ModeSwicherTask;
import vn.cybersoft.obs.android.utilities.ReflectionUtils;
import android.R.anim;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class OptimalModeFragment extends Fragment implements OnItemClickListener, ViewBinder, ModeSwitcherListener {
	public static final String t = "OptimalModeFragment";
	public static final int LAYOUT_ID = R.layout.optimal_mode_fragment;
	
	// Request codes for returning data from specified intent.
	public static final int CONFIRM_CHANGE_MODE = 1;
	
	private ListView listMode;
	private ModeSwicherTask mModeSwicherTask;
	
	private int mSelectedPosition = -1;
	private long mPendingId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(LAYOUT_ID, container, false);
		
		Cursor c = OBS.getInstance().getModeDbAdapter().fetchAllMode();
		
		String[] data = new String[] { ModeDbAdapter.NAME, ModeDbAdapter.DESC, ModeDbAdapter.CAN_EDIT };
		int[] view = new int[] { R.id.modeName, R.id.modeDesc, R.id.editMode };
		SimpleCursorAdapter adapter = 
				new SimpleCursorAdapter(getActivity(), 
						R.layout.optimal_mode_list_row, c, data, view, CursorAdapter.NO_SELECTION);
		adapter.setViewBinder(this); 
		
		if(adapter.getCount() > 0) {
			listMode = (ListView) rootView.findViewById(R.id.listMode);
			listMode.setAdapter(adapter);
			listMode.setOnItemClickListener(this);
		}
		
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		//DialogUtils.showMessageAsPopup(getActivity(), ((TextView)view.findViewById(R.id.modeName)).getText().toString(), "");
		
		mPendingId = id;
		
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		
		if(c.getCount() > 1) {
			Log.e(t, "Error: bad optimal mode query");
		}
		
		String brightness = (c.getInt(c.getColumnIndex(ModeDbAdapter.SCREEN_BRIGHTNESS)) * 100) / 250 + "%";
		String timeout = c.getInt(c.getColumnIndex(ModeDbAdapter.SCREEN_TIMEOUT)) + " " + getString(R.string.seconds);
		String vibrate = Boolean.parseBoolean(
				c.getString(c.getColumnIndex(ModeDbAdapter.VIBRATE))) ? getString(R.string.on) : getString(R.string.off);
		String wifi = Boolean.parseBoolean(
				c.getString(c.getColumnIndex(ModeDbAdapter.WIFI))) ? getString(R.string.on) : getString(R.string.off);
		String bluetooth = Boolean.parseBoolean(
				c.getString(c.getColumnIndex(ModeDbAdapter.BLUETOOTH))) ? getString(R.string.on) : getString(R.string.off);
		String sync = Boolean.parseBoolean(
				c.getString(c.getColumnIndex(ModeDbAdapter.SYNC))) ? getString(R.string.on) : getString(R.string.off);
		String haptic = Boolean.parseBoolean(
				c.getString(c.getColumnIndex(ModeDbAdapter.HAPTIC_FEEDBACK))) ? getString(R.string.on) : getString(R.string.off);

		
		Intent i = new Intent(getActivity(), SwitchModeConfirmActivity.class);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_TITLE, ((TextView)view.findViewById(R.id.modeName)).getText().toString());
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_BRIGHTNESS, brightness);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_SCREEN_TIMEOUT, timeout);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_VIBRATE, vibrate);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_WIFI, wifi);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_BLUETOOTH, bluetooth);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_SYNC, sync);
		i.putExtra(SwitchModeConfirmActivity.KEY_EXTRA_HAPTIC, haptic);
		getActivity().startActivityForResult(i, CONFIRM_CHANGE_MODE);
		
	}
	
	@Override
	public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
		boolean canEdit = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(ModeDbAdapter.CAN_EDIT)));
		
		if(view instanceof TextView) {
			if(!canEdit) {
				int resId = ReflectionUtils.getResourceId(cursor.getString(columnIndex), R.string.class);
				String text = resId != -1 ? getString(resId) : cursor.getString(columnIndex);
				((TextView) view).setText(text);
				return true;
			}
			return false;
		}
		
		if(view.getId() == R.id.editMode) {
			if(canEdit) {
				view.setVisibility(View.VISIBLE); 
				view.setEnabled(true); 
				view.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					}
				});
			} else {
				view.setEnabled(false); 
			}
			return true;
		}
		
/*		if(view.getId() == R.id.selected) {
			RadioButton radio = (RadioButton) view.findViewById(R.id.selected);
			radio.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mSelectedPosition = (Integer) v.getTag();
					((BaseAdapter)listMode.getAdapter()).notifyDataSetInvalidated();
					
				}
			});
			return true;
		}*/
		
		return true;
	}

	@Override
	public void switchComplete() {
		// TODO Auto-generated method stub
		mModeSwicherTask.setModeSwitcherListener(null);
		ModeSwicherTask t = mModeSwicherTask;
		mModeSwicherTask = null;
		t.cancel(true);
	}

	@Override
	public void switchError(String errorMsg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case CONFIRM_CHANGE_MODE:
			if(resultCode == Activity.RESULT_OK) {
				if(mModeSwicherTask == null) {
					mModeSwicherTask = new ModeSwicherTask();
					mModeSwicherTask.setModeSwitcherListener(this);
					mModeSwicherTask.execute(mPendingId);
				} else {
					Log.e(t, "Error: SwitchOptimalModeTask is busy !");
				}
			}
		
			break;

		default:
			break;
		}
		
	}

}
