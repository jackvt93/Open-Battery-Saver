/*
 * Copyright (C) 2011 University of Washington
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

import vn.cybersoft.obs.android.activities.MainActivity;
import vn.cybersoft.obs.android.activities.R;
import vn.cybersoft.obs.android.activities.MainActivity.ScreenList;
import vn.cybersoft.obs.android.adapters.MainMenuItemAdapter;
import vn.cybersoft.obs.android.models.MainMenuItem;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author Luan Vu
 * 
 */
public class MainMenuFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = "MainMenuFragment";
	
	private static final int LAYOUT_ID = R.layout.main_menu_fragment;

	public static final int MENU_BATTERY_INFO = 0x001;
	public static final int MENU_OPTIMIZATION = 0x002;
	public static final int MENU_CHARGE = 0x003;
	public static final int MENU_CONSUMPTION = 0x004;
	public static final int MENU_ABOUT = 0x005;

	private ListView mMenu;
	private ListView mSecondMenu;
	
	private View mLastMenuView;
	private int mLastMenuColor;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);

		mMenu = (ListView) view.findViewById(R.id.menuList);
		MainMenuItemAdapter adapter = new MainMenuItemAdapter(getActivity(),
				R.layout.main_menu_list_row);
		adapter.add(new MainMenuItem(MENU_BATTERY_INFO,
				R.drawable.ic_action_battery,
				getString(R.string.menu_battery_info)));
		adapter.add(new MainMenuItem(MENU_OPTIMIZATION,
				R.drawable.ic_action_flash_on,
				getString(R.string.menu_caption_optimization)));
		adapter.add(new MainMenuItem(MENU_CHARGE, R.drawable.ic_action_battery,
				getString(R.string.menu_caption_charge)));
		adapter.add(new MainMenuItem(MENU_CONSUMPTION, R.drawable.ic_action_battery,
				getString(R.string.menu_caption_consumption)));
		mMenu.setAdapter(adapter);
		mMenu.setOnItemClickListener(this);
		
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Activity activity = getActivity();
		
		if(null == activity) {
			return;
		}
		
		if(activity instanceof MainActivity) {
			MainMenuItemAdapter adapter = (MainMenuItemAdapter)parent.getAdapter();
			if(mLastMenuView != null) {
				mLastMenuView.setBackgroundColor(mLastMenuColor);
				view.setBackgroundColor(getResources().getColor(R.color.nephritis)); 
				mLastMenuView = view;
			} else {
				view.setBackgroundColor(getResources().getColor(R.color.nephritis)); 
			}
			switch (adapter.getItem(position).id) {
			case MENU_BATTERY_INFO:
				((MainActivity)activity).swapToFragmentView(ScreenList.BATTERY_INFO);
				break;
			case MENU_OPTIMIZATION:
				((MainActivity)activity).swapToFragmentView(ScreenList.OPTIMIZATION);
				break;

			case MENU_CHARGE:
				((MainActivity)activity).swapToFragmentView(ScreenList.CHARGE);
				break;

			case MENU_CONSUMPTION:
				((MainActivity)activity).swapToFragmentView(ScreenList.CONSUMPTION);
				break;

			case MENU_ABOUT:
				((MainActivity)activity).swapToFragmentView(ScreenList.ABOUT);
				break;
			}
		}
		

	}

}
