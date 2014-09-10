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
package vn.cybersoft.obs.android.utilities;

import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.provider.OptimalMode;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;

/**
 * @author Van Chung & Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class DeviceUtils {
	public final static String t = DeviceUtils.class.getSimpleName();
	
	public static void switchToOptimalMode(OptimalMode mode) {
		if(null == mode) {
			if (Log.LOGV) {
				Log.v(t + ".switchToOptimalMode(): mode to switch is null"); 
			}
			return;
		}
		
		Log.i(t + ".switchToOptimalMode(): switch to optimal mode: " + mode.name);
		
		switchBrightness(mode.screenBrightness);
		setScreenTimeOut(mode.screenTimeout);
		switchWifi(mode.wifi);
		switchBluetooth(mode.bluetooth);
		switchSync(mode.sync);
		
	}
	
	
	public static void switchWifi(boolean value){
		if (Log.LOGV) {
			Log.v(t + ".switchWifi(): value = " + value);
		}
		
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
		if (Log.LOGV) {
			Log.v(t + ".switchBluetooth(): value = " + value);
		}
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
		if (Log.LOGV) {
			Log.v(t + ".switchBrightness(): value = " + value);
		}
		Settings.System.putInt(OBS.getInstance().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, value);
	}
	
	//change screen lock time out
	public static void setScreenTimeOut(int value){
		if (Log.LOGV) {
			Log.v(t + ".setScreenTimeOut(): value = " + value);
		}
		Settings.System.putInt(OBS.getInstance().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
	}
	
	//set location mode
	public static void setLocationMode(int mode){
		if (Log.LOGV) {
			Log.v(t + ".setLocationMode(): mode = " + mode);
		}
		Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
		intent.putExtra("enable", mode);
		OBS.getInstance().sendBroadcast(intent,android.Manifest.permission.WRITE_SECURE_SETTINGS);
	}
	
	//switch Sync on/off
	public static void switchSync(boolean value){
		if (Log.LOGV) {
			Log.v(t + ".switchSync(): value = " + value);
		}
		//get state of Sync
		String authority = "com.google.provider";
		AccountManager am = AccountManager.get(OBS.getInstance().getApplicationContext());
		Account account = am.getAccountsByType("com.google")[0];
		
	}
	
	public long getBatteryTimeLeftAtMillis() {
		return 0;
	}
	
	
}
