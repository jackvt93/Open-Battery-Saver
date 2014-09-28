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
import vn.cybersoft.obs.android.activities.SetOptimalModeActivity;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.dialog.SwitchModeConfirmDialog;
import vn.cybersoft.obs.android.listeners.ModeSwitcherListener;
import vn.cybersoft.obs.android.provider.OptimalMode;
import vn.cybersoft.obs.android.tasks.ModeSwitcherTask;
import vn.cybersoft.obs.android.utilities.ToastManager;
import vn.cybersoft.obs.android.utilities.Utils;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ModeTabFragment extends Fragment implements OnItemClickListener, LoaderCallbacks<Cursor>, ModeSwitcherListener {
	public static final String t = "OptimalModeFragment";
	public static final int LAYOUT_ID = R.layout.mode_optimization_fragment;
	
	private static final int OPTIMAL_MODE_LIST_LOADER = 0x02;
	
	private ModeSwitcherTask mModeSwicherTask;
	private LayoutInflater mInflater;
	private ListView mModeList;
	private OptimalModeAdapter mAdapter;
	private Loader<Cursor> mCursorLoader = null; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCursorLoader = getLoaderManager().initLoader(OPTIMAL_MODE_LIST_LOADER, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		final View rootView = mInflater.inflate(LAYOUT_ID, container, false);
		mModeList = (ListView) rootView.findViewById(R.id.modes_list); 
		
		mAdapter = new OptimalModeAdapter(getActivity(), R.layout.mode_optimization_list_row);
		
		mModeList.setAdapter(mAdapter);
		mModeList.setVerticalScrollBarEnabled(true);
		mModeList.setOnCreateContextMenuListener(this);
		mModeList.setOnItemClickListener(this);
		
        View addSchedule = rootView.findViewById(R.id.add_new_mode);
        addSchedule.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    addNewMode();
                }
            });
        // Make the entire view selected when focused.
        addSchedule.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setSelected(hasFocus);
                }
        });
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mCursorLoader != null && mCursorLoader.isStarted()) {
			mCursorLoader.forceLoad();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ToastManager.cancelToast();
	}
	
	private void addNewMode() {
		startActivity(new Intent(getActivity(), SetOptimalModeActivity.class));
	}
	
	private void editMode(long id) {
        Intent intent = new Intent(getActivity(), SetOptimalModeActivity.class);
        intent.putExtra(OptimalMode.EXTRA_ID, id);
        startActivity(intent);
	}
	
	private void switchToMode(long id) {
		if(mModeSwicherTask == null) {
			mModeSwicherTask = new ModeSwitcherTask();
			mModeSwicherTask.setModeSwitcherListener(ModeTabFragment.this);
			mModeSwicherTask.execute(id);
		} else {
			mModeSwicherTask.setModeSwitcherListener(null);
			ModeSwitcherTask t = mModeSwicherTask; 
			mModeSwicherTask = null;
			t.cancel(true);
		}
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
					switchToMode(optimalMode.id);
				}
			});
			dialog.setNegativeButton(R.string.cancel, null); 
		}
		dialog.show();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// Inflate the menu from xml.
		getActivity().getMenuInflater().inflate(R.menu.optimal_mode_context_menu, menu); 
		
        // Use the current item to create a custom view for the header.
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        
        final Cursor c = (Cursor) mModeList.getAdapter().getItem((int) info.position);
        
        OptimalMode optimalMode = new OptimalMode(c);
        
        menu.setHeaderTitle(optimalMode.name);
        
        if (!optimalMode.canEdit) {
        	// the original mode can't modify or delete
			menu.findItem(R.id.edit_mode).setEnabled(false).setVisible(false); 
			menu.findItem(R.id.delete_mode).setEnabled(false).setVisible(false);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
        case R.id.delete_mode:
            // Confirm that the schedule will be deleted.
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.delete_mode))
                    .setMessage(getString(R.string.delete_mode_confirm))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                	OptimalMode.deleteMode(getActivity().getContentResolver(), id);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;

        case R.id.switch_mode:
            switchToMode(id); 
            return true;

        case R.id.edit_mode:
        	editMode(id);
            return true;

        default:
            break;
        }
		return super.onContextItemSelected(item);
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
	
    /**
     * Scroll to mode with given mode id.
     *
     * @param modeId The mode id to scroll to.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    private void scrollToMode(long modeId) {
        int modePosition = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            long id = mAdapter.getItemId(i);
            if (id == modeId) {
            	modePosition = i;
                break;
            }
        }

        if (modePosition >= 0) {
            mModeList.smoothScrollToPositionFromTop(modePosition, 0);
        } else {
            // Trying to display a deleted mode should only happen from a missed notification for
            // an mode that has been marked deleted after use.
            Context context = getActivity().getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.missed_mode_has_been_deleted, Toast.LENGTH_LONG);
            ToastManager.setToast(toast);
            toast.show();
        }
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
			
			if (mode.desc != null) {
				modeDesc.setText(Utils.getString(mContext, mode.desc, R.string.class));
			} else {
				modeName.setPadding(0, 15, 0, 15); 
				modeDesc.setVisibility(View.GONE); 
			}
			
			
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
