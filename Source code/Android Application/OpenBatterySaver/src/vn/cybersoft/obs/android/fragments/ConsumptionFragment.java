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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.Arrays;

import vn.cybersoft.obs.andriod.batterystats2.service.BatteryStats;
import vn.cybersoft.obs.andriod.batterystats2.service.ICounterService;
import vn.cybersoft.obs.andriod.batterystats2.service.UidInfo;
import vn.cybersoft.obs.andriod.batterystats2.util.Counter;
import vn.cybersoft.obs.andriod.batterystats2.util.SystemInfo;
import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.preference.PowerGaugePreference;
import vn.cybersoft.obs.android.utilities.Log;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceGroup;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConsumptionFragment extends PreferenceFragment {
	private static final String TAG = "ConsumptionFragment";
	public static final int LAYOUT_ID = R.layout.consumption_fragment; 
	
    private static final String KEY_APP_LIST = "app_list";
	
	private static final double HIDE_UID_THRESHOLD = 0.1;
	
	private ICounterService mCounterService;
	
    private PreferenceGroup mAppListGroup;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(getString(R.string.app_name) + 
				" > " + getString(R.string.consumption));
		
		mCounterService = OBS.getInstance().getCounterService();
		if (mCounterService == null) {
			Log.e("in " + TAG + "Can't get counter service !!!!");
		}
		
        addPreferencesFromResource(R.xml.power_usage_summary);
        mAppListGroup = (PreferenceGroup) findPreference(KEY_APP_LIST);
        setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshStats();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);
		return view;
	}
	
	private void refreshStats() {
		mAppListGroup.removeAll();
        mAppListGroup.setOrderingAsAdded(false);
		try {
			int noUidMask = mCounterService.getNoUidMask();
			byte[] rawUidInfo = mCounterService.getUidInfo(Counter.WINDOW_TOTAL, noUidMask | 0);
			
			if (rawUidInfo != null) {
		        UidInfo[] uidInfos = (UidInfo[]) new ObjectInputStream(new ByteArrayInputStream(rawUidInfo)).readObject();
		        double total = 0;
		        for (UidInfo uidInfo : uidInfos) {
					if (uidInfo.uid == SystemInfo.AID_ALL) 
						continue;
		            uidInfo.key = uidInfo.totalEnergy;
		            uidInfo.unit = "J";
		            total += uidInfo.key;
				}
		        
		        if (total == 0) {
					total = 1;
				}
		        
		        for (UidInfo uidInfo : uidInfos) {
					uidInfo.percentage = (uidInfo.key / total) * 100;
				}
		        
		        Arrays.sort(uidInfos);
		        
		        for (int i = 0; i < uidInfos.length; i++) {
		        	UidInfo info = uidInfos[i];
		        	final double percentOfTotal = info.percentage;
					if (info.uid == SystemInfo.AID_ALL ||
						percentOfTotal < HIDE_UID_THRESHOLD) { 
						continue;
					}
					
					PackageManager packageManager = getActivity().getPackageManager();
					SystemInfo systemInfo = SystemInfo.getInstance();
					
					String name = systemInfo.getUidName(info.uid, packageManager);
					
					if (name.equals(getString(R.string.app_name))) {
						continue;
					}
					
					Drawable icon = systemInfo.getUidIcon(info.uid, packageManager);
					
					PowerGaugePreference pref = new PowerGaugePreference(getActivity(), icon, info);
					 
					pref.setTitle(name);
					pref.setOrder(Integer.MAX_VALUE - (int) info.key); 
					pref.setPercent(percentOfTotal, percentOfTotal);
					pref.setKey(Integer.toString(info.uid)); 
					mAppListGroup.addPreference(pref);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
