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

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.concurrent.Callable;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.listeners.Callback;
import vn.cybersoft.obs.android.models.BatteryInfo;
import vn.cybersoft.obs.android.utilities.Log;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Luan Vu
 *
 */
public class BatteryInfoFragment extends Fragment {
	private static final String t = BatteryInfoFragment.class.getSimpleName();
	public static final int LAYOUT_ID = R.layout.battery_info_fragment; 
	
	private IncomeinHandler mHandler = new IncomeinHandler(this); 
	
	private BatteryInfo mBatteryInfo;
	private BatteryInfoReceiver mBatteryInfoReceiver;
	
	private TextView mTemperature, mVoltage, mCapacity, mTimeLeftHour, mTimeLeftMin;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);
		
		mTemperature = (TextView) view.findViewById(R.id.temperature_text);
		mVoltage = (TextView) view.findViewById(R.id.voltage_text);
		mCapacity = (TextView) view.findViewById(R.id.capacity_text);
		
		mCapacity.setText(getBatteryCapacity()+ ""); 
		
		mBatteryInfo = new BatteryInfo();
		mBatteryInfoReceiver = new BatteryInfoReceiver();
		
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.v(t + ".onStart()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(t + ".onResume()");
		getActivity().registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(t + ".onPause()");
		getActivity().unregisterReceiver(mBatteryInfoReceiver); 
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.v(t + ".onStop()");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(t + ".onDestroyView()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(t + ".onDestroy()");
	}
	
	private void updateLayout() {
		mTemperature.setText((float)mBatteryInfo.temperature / 10 + "");
		mVoltage.setText((float)mBatteryInfo.voltage / 1000 + "");
	}
	
	class BatteryInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Log.LOGV) { 
				Log.v(t + ".BatteryInfoReceiver.onReceive()"); 
			}
			
			Bundle data = intent.getExtras();
			mBatteryInfo.status = data.getInt(BatteryManager.EXTRA_STATUS);
			mBatteryInfo.heath = data.getInt(BatteryManager.EXTRA_HEALTH);
			mBatteryInfo.present = data.getBoolean(BatteryManager.EXTRA_PRESENT);
			mBatteryInfo.level = data.getInt(BatteryManager.EXTRA_LEVEL);
			mBatteryInfo.scale = data.getInt(BatteryManager.EXTRA_SCALE);
			mBatteryInfo.iconSmall = data.getInt(BatteryManager.EXTRA_ICON_SMALL);
			mBatteryInfo.plugged = data.getInt(BatteryManager.EXTRA_PLUGGED);
			mBatteryInfo.voltage = data.getInt(BatteryManager.EXTRA_VOLTAGE);
			mBatteryInfo.temperature = data.getInt(BatteryManager.EXTRA_TEMPERATURE);
			mBatteryInfo.technology = data.getString(BatteryManager.EXTRA_TECHNOLOGY);
			mBatteryInfo.invalidCharger = data.getInt("invalid_charger");
			
			mHandler.sendEmptyMessage(0);
		}
		
	}
	
	/*
	 * Used to prevent memory leaks
	 */
	static class IncomeinHandler extends Handler {
		private final WeakReference<BatteryInfoFragment> mTarget;
		
		IncomeinHandler(BatteryInfoFragment target) {
			mTarget = new WeakReference<BatteryInfoFragment>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			BatteryInfoFragment target = mTarget.get();
			if (target != null) {
				target.updateLayout();
			}
		}
	}
	
	private double getBatteryCapacity() {
		Object powerProfile = null;
		final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
		
		try {
			powerProfile = Class.forName(POWER_PROFILE_CLASS)
					.getConstructor(android.content.Context.class).newInstance(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double ret = 0;
		try {
			ret = (Double) Class.forName(POWER_PROFILE_CLASS)
					.getMethod("getBatteryCapacity")
					.invoke(powerProfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
