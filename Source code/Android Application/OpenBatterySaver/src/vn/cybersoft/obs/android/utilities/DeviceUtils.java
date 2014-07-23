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
package vn.cybersoft.obs.android.utilities;

import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.models.OptimalMode;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

/**
 * @author Van Chung & Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class DeviceUtils {
	public final static String t = "DeviceUtils";
	
	public static void switchToOptimalMode(OptimalMode mode) {
		if(null == mode) {
			Log.e(t, "Error: Switch to null optimal mode");
			return;
		}
		
		switchBrightness(mode.screenBrightness);
		switchScreenTimeOut(mode.screenTimeout);
		switchWifi(mode.wifi);
		switchBluetooth(mode.bluetooth);
		switchSync(mode.sync);
		
		Log.i(t, "Switch to optimal mode: " + mode.name);
	}
	
	
	public static void switchWifi(boolean value){
		Log.i(t, "Switch wifi: " + value);
		WifiManager wifi = (WifiManager)OBS.getInstance()
				.getSystemService(Context.WIFI_SERVICE);
		int state = wifi.getWifiState();
		//wifi is disable and must turning on
		if(state == 1 && value == true){
			wifi.setWifiEnabled(value);
		}
		//wifi is enable and must turning off
		if(state == 3 && value == false){
			wifi.setWifiEnabled(false);
		}
	}
	
	public static void switchBluetooth(boolean value){
		Log.i(t, "Switch bluetooth: " + value);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter != null){
			int state = bluetoothAdapter.getState();
			//bluetooth is off and must turning on
			if(state == 10 && value == true){
				bluetoothAdapter.enable();
			}
			//bluetooth is off and must turning off
			if(state == 12 && value == false){
				bluetoothAdapter.disable();
			}
		}
	}
	
	//change screen brightness
	public static void switchBrightness(int value){
		Log.i(t, "Switch brightness  " + value);
	    	Settings.System.putInt(OBS.getInstance().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, value);
	}
	
	//change screen lock time out
	public static void switchScreenTimeOut(int value){
		Log.i(t, "Switch screen timeout: " + value);
		Settings.System.putInt(OBS.getInstance().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
	}
	
	//set location mode
	public static void setLocationMode(int mode){
		Log.i(t, "Switch localtion mode: " + mode);
		Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
		intent.putExtra("enable", mode);
		OBS.getInstance().sendBroadcast(intent,android.Manifest.permission.WRITE_SECURE_SETTINGS);
	}
	
	//switch Sync on/off
	public static void switchSync(boolean value){
		Log.i(t, "Switch sync: " + value);
		//get state of Sync
		String authority = "com.google.provider";
		AccountManager am = AccountManager.get(OBS.getInstance().getApplicationContext());
		Account account = am.getAccountsByType("com.google")[0];
		
	}
	
	
}
