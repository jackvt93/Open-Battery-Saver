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
package vn.cybersoft.obs.android.fragments;


import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.dialog.SwitchModeConfirmDialog;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.provider.OptimalMode;
import vn.cybersoft.obs.android.tasks.ModeSwitcherTask;
import vn.cybersoft.obs.android.utilities.Utils;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.TwoStatePreference;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ModeTabFragment extends Fragment implements OnItemClickListener, LoaderCallbacks<Cursor>, ModeSwitcherListener {
	public static final String t = "OptimalModeFragment";
	public static final int LAYOUT_ID = R.layout.mode_optimization_fragment;
	
	private static final int OPTIMAL_MODE_LIST_LOADER = 0x02;
	
	private ModeSwitcherTask mModeSwicherTask;
	private LayoutInflater mInflater;
	private ListView mModeList;
	private OptimalModeAdapter mAdapter;
	private Cursor mCursor;
	private Loader mCursorLoader = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mCursorLoader = getLoaderManager().initLoader(OPTIMAL_MODE_LIST_LOADER, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		final View rootView = mInflater.inflate(LAYOUT_ID, container, false);
		
		mAdapter = new OptimalModeAdapter(getActivity(), R.layout.mode_optimization_list_row);
		
		mModeList = (ListView) rootView.findViewById(R.id.modes_list); 
		mModeList.setVerticalScrollBarEnabled(true);
		mModeList.setOnCreateContextMenuListener(this);
		mModeList.setOnItemClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		final OptimalMode optimalMode = new OptimalMode(c);
				
		SwitchModeConfirmDialog dialog = new SwitchModeConfirmDialog(getActivity(), optimalMode); 
		dialog.setTitle(((TextView) view.findViewById(R.id.modeName)).getText().toString()); 
		if (optimalMode.id != OBS.getSelectedOptimalModeId()) {
			dialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mModeSwicherTask == null) {
						mModeSwicherTask = new ModeSwitcherTask();
						mModeSwicherTask.setModeSwitcherListener(ModeTabFragment.this);
						mModeSwicherTask.execute(optimalMode.id);
					} else {
						mModeSwicherTask.setModeSwitcherListener(null);
						ModeSwitcherTask t = mModeSwicherTask; 
						mModeSwicherTask = null;
						t.cancel(true);
					}
				}
			});
			dialog.setNegativeButton(R.string.cancel, null); 
		}
		dialog.show();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
	}
	
	@Override
	public void switchComplete() {
		mAdapter.notifyDataSetChanged();
		
		mModeSwicherTask.setModeSwitcherListener(null);
		ModeSwitcherTask t = mModeSwicherTask;
		mModeSwicherTask = null;
		t.cancel(true);
	}

	@Override
	public void switchError(String errorMsg) {
		
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		return OptimalMode.getModesCursorLoader(getActivity());
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private class OptimalModeAdapter extends ResourceCursorAdapter {
		/**
		 * @param context
		 * @param layout
		 * @param c
		 */
		public OptimalModeAdapter(Context context, int layout) {
			super(context, layout, null, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			final OptimalMode mode = new OptimalMode(c);
			
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.selected);
			
			if (mode.id == OBS.getSelectedOptimalModeId()) {
				radioButton.setChecked(true); 
			} else {
				radioButton.setChecked(false);
			}
			
			TextView modeName = (TextView) view.findViewById(R.id.modeName);
			modeName.setText(Utils.getString(mContext, mode.name, R.string.class)); 
			
			TextView modeDesc = (TextView) view.findViewById(R.id.modeDesc);
			modeDesc.setText(Utils.getString(mContext, mode.desc, R.string.class));
			
			ImageView editMode = (ImageView) view.findViewById(R.id.editMode);
			if (mode.canEdit) {
				editMode.setVisibility(View.VISIBLE);
				editMode.setEnabled(true);
				editMode.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					}
				});
			} else {
				editMode.setEnabled(false);
			}
		}
	}

}
