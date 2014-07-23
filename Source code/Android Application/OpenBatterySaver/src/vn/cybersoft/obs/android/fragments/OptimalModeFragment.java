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
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.database.ModeDbAdapter;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.tasks.ModeSwicherTask;
import vn.cybersoft.obs.android.utilities.ReflectionUtils;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OptimalModeFragment extends Fragment implements OnItemClickListener, ViewBinder, ModeSwitcherListener {
	public static final String t = "OptimalModeFragment";
	public static final int LAYOUT_ID = R.layout.optimal_mode_fragment;
	
	private ListView listMode;
	private ModeSwicherTask mModeSwicherTask;
	
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
		if(mModeSwicherTask == null) {
			mModeSwicherTask = new ModeSwicherTask();
			mModeSwicherTask.setModeSwitcherListener(this);
			mModeSwicherTask.execute(id);
		} else {
			Log.e(t, "Error: SwitchOptimalModeTask is busy !");
		}
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
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
			}
		}
		
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

}
