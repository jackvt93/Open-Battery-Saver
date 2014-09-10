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
import vn.cybersoft.obs.android.fragments.AboutFragment;
import vn.cybersoft.obs.android.fragments.BatteryInfoFragment;
import vn.cybersoft.obs.android.fragments.ChargeFragment;
import vn.cybersoft.obs.android.fragments.ConsumptionFragment;
import vn.cybersoft.obs.android.fragments.MainMenuFragment;
import vn.cybersoft.obs.android.fragments.OptimizationFragment;
import vn.cybersoft.obs.android.listeners.Callback;
import vn.cybersoft.obs.android.models.BatteryInfo;
import vn.cybersoft.obs.android.receivers.TimeScheduleReceiver;
import vn.cybersoft.obs.android.services.BatteryStatusService;
import vn.cybersoft.obs.android.utilities.Utils;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class MainActivity extends BaseActivity {
	private static final String t = "MainActivity";
	
	public static enum ScreenList {
		BATTERY_INFO,
		OPTIMIZATION, 
		CHARGE, 
		CONSUMPTION, 
		ABOUT
	};
	
	// tags for retained context
	private static final String CURRENT_FRAGMENT = "currentFragment";
	
	private ScreenList currentFragment = ScreenList.BATTERY_INFO;
	

	public MainActivity() {
		super(R.string.app_name);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(null != savedInstanceState) {
			currentFragment = ScreenList.valueOf(savedInstanceState.containsKey(CURRENT_FRAGMENT) ? 
						savedInstanceState.getString(CURRENT_FRAGMENT) : currentFragment.name());
		}
		
		// set the Behind View
		setBehindContentView(R.layout.behind_layout);
		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.behind_content, new MainMenuFragment())
								   .commit();
								   
		setContentView(R.layout.main_layout); 
		
		// customize the SlidingMenu
		getSlidingMenu().setMode(SlidingMenu.LEFT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN); 
		setSlidingActionBarEnabled(false);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(fragmentManager.getBackStackEntryCount() == 0) {
			swapToFragmentView(currentFragment);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENT_FRAGMENT, currentFragment.name());
	}
	
	@Override
	public void onBackPressed() {
		FragmentManager mgr = getSupportFragmentManager();
		 int idxLast = mgr.getBackStackEntryCount() - 2;
		 if(idxLast < 0) {
			 this.setResult(RESULT_OK); 
		     finish();
		 } else {
			 BackStackEntry entry = mgr.getBackStackEntryAt(idxLast);
			 swapToFragmentView(ScreenList.valueOf(entry.getName()));
		 }
	}
	
	public void swapToFragmentView(ScreenList newFragment) {
		Log.i(t, "swapToFragmentView: " + newFragment.toString());
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = null; 
		if(ScreenList.BATTERY_INFO == newFragment) {
			fragment = fragmentManager.findFragmentById(BatteryInfoFragment.LAYOUT_ID);
			if(null == fragment) {
				fragment = new BatteryInfoFragment();
			}
		} else if(ScreenList.OPTIMIZATION == newFragment) {
			fragment = fragmentManager.findFragmentById(OptimizationFragment.LAYOUT_ID);
			if(null == fragment) {
				fragment = new OptimizationFragment();
			}
		} else if(ScreenList.CHARGE == newFragment) {
			fragment = fragmentManager.findFragmentById(ChargeFragment.LAYOUT_ID);
			if(null == fragment) {
				fragment = new ChargeFragment();
			}
		} else if(ScreenList.CONSUMPTION == newFragment) {
			fragment = fragmentManager.findFragmentById(ConsumptionFragment.LAYOUT_ID);
			if(null == fragment) {
				fragment = new ConsumptionFragment();
			}
		} else if(ScreenList.ABOUT == newFragment) {
			fragment = fragmentManager.findFragmentById(AboutFragment.LAYOUT_ID);
			if(null == fragment) {
				fragment = new AboutFragment();
			}
		} else {
			throw new IllegalStateException("Unrecognized ScreenList type");
		}
		
		currentFragment = newFragment;
		BackStackEntry entry = null;
		for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
			BackStackEntry e = fragmentManager.getBackStackEntryAt(i);
			if (e.getName().equals(currentFragment.name())) {
				entry = e;
				break;
			}
		}
		
		if(null != entry) {
			// flush backward, including the screen want to go back to
			fragmentManager.popBackStackImmediate(currentFragment.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		
		// add transaction to show the screen we want
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.main_frame, fragment);
		transaction.addToBackStack(currentFragment.name());
		transaction.commit();
		levelSafeInvalidateOptionsMenu();
	}
	
	  /**
	   * Android Lint complains, but we are using Sherlock,
	   * so this does exist for down-level devices.
	   */
	  @SuppressLint("NewApi") 
	  private void levelSafeInvalidateOptionsMenu() {
	    invalidateOptionsMenu();
	  }

}
