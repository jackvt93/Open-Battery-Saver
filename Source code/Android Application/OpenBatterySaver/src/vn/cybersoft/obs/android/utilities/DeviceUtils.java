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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.provider.OptimalMode;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * @author Van Chung & Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class DeviceUtils {
	public final static String t = DeviceUtils.class.getSimpleName();

	private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
	private static final int FALLBACK_SCREEN_BRIGHTNESS_VALUE = 255;

	public static void switchToOptimalMode(OptimalMode mode) {
		if (null == mode) {
			if (Log.LOGV) {
				Log.v(t + ".switchToOptimalMode(): mode to switch is null");
			}
			return;
		}

		Log.i(t + ".switchToOptimalMode(): switch to optimal mode: " + mode.name);

		setScreenBrightness(OBS.getInstance().getContentResolver(),
				mode.screenBrightness);
		setScreenTimeOut(OBS.getInstance().getContentResolver(),
				mode.screenTimeout);
		turnOnWifi(OBS.getInstance(), mode.wifi);
		turnOnBluetooth(mode.bluetooth);
		switchSync(mode.sync);

	}

	public static void turnOnWifi(Context context, boolean ON) {
		if (Log.LOGV) {
			Log.v(t + ".turnOnWifi(): ON = " + ON);
		}

		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		int state = wifi.getWifiState();
		// wifi is disable and must turning on
		if (state == 1 && ON == true) {
			wifi.setWifiEnabled(ON);
		}
		// wifi is enable and must turning off
		if (state == 3 && ON == false) {
			wifi.setWifiEnabled(false);
		}
	}

	public static void turnOnBluetooth(boolean ON) {
		if (Log.LOGV) {
			Log.v(t + ".turnOnBluetooth(): ON = " + ON);
		}
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			int state = bluetoothAdapter.getState();
			// bluetooth is off and must turning on
			if (state == 10 && ON == true) {
				bluetoothAdapter.enable();
			}
			// bluetooth is off and must turning off
			if (state == 12 && ON == false) {
				bluetoothAdapter.disable();
			}
		}
	}

	// change screen brightness
	public static void setScreenBrightness(ContentResolver contentResolver, int value) {
		if (Log.LOGV) {
			Log.v(t + ".setScreenBrightness(): value = " + value);
		}
		Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value);
	}

	/**
	 * @param contentResolver
	 * @param value
	 */
	public static void setScreenTimeOut(ContentResolver contentResolver, int value) {
		if (Log.LOGV) {
			Log.v(t + ".setScreenTimeOut(): value = " + value);
		}
		Settings.System.putInt(contentResolver,
				Settings.System.SCREEN_OFF_TIMEOUT, value);
	}

	// set location mode
	public static void setLocationMode(int mode) {
		if (Log.LOGV) {
			Log.v(t + ".setLocationMode(): mode = " + mode);
		}
		Intent intent = new Intent(
				"com.android.settings.location.MODE_CHANGING");
		intent.putExtra("enable", mode);
		OBS.getInstance().sendBroadcast(intent,
				android.Manifest.permission.WRITE_SECURE_SETTINGS);
	}

	// switch Sync on/off
	public static void switchSync(boolean value) {
		if (Log.LOGV) {
			Log.v(t + ".switchSync(): value = " + value);
		}
		// get state of Sync
		String authority = "com.google.provider";
		AccountManager am = AccountManager.get(OBS.getInstance()
				.getApplicationContext());
		Account account = am.getAccountsByType("com.google")[0];

	}

	public long getBatteryTimeLeftAtMillis() {
		return 0;
	}

	public static int getScreenTimeoutInMillis() {
		return Settings.System.getInt(OBS.getInstance().getContentResolver(),
				Settings.System.SCREEN_OFF_TIMEOUT,
				FALLBACK_SCREEN_TIMEOUT_VALUE);
	}

	public static int getScreenBrightness() {
		return Settings.System.getInt(OBS.getInstance().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS,
				FALLBACK_SCREEN_BRIGHTNESS_VALUE);
	}

	public static int getCurrentBatteryLevel(Context context) {
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		return level * 100 / scale;
	}

	public static int getBatteryScale(Context context) {
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		return scale;
	}

	// hack code to get battery capacity
	public static double getBatteryCapacity(Context context) {
		Object powerProfile = null;
		final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

		try {
			powerProfile = Class.forName(POWER_PROFILE_CLASS)
					.getConstructor(android.content.Context.class)
					.newInstance(context);
		} catch (Exception e) {
			e.printStackTrace();
		}

		double ret = 0;
		try {
			ret = (Double) Class.forName(POWER_PROFILE_CLASS)
					.getMethod("getBatteryCapacity").invoke(powerProfile);
		} catch (Exception e) {
			if (Log.LOGV) {
				Log.v(t + ".getBatteryCapacity(): Error: " + e.getMessage());
			}
			//e.printStackTrace();
		}
		return ret;
	}
	
	// http://stackoverflow.com/questions/13523396/enable-disable-mobile-data-gprs-using-code
	public static void turnOnDataConnection(Context context, boolean ON) {
		if (Log.LOGV) {
			Log.v(t + ".turnOnDataConnection(): ON = " + ON);
		}
		try {
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) {
				Method dataConnSwitchmethod;
				Class<?> telephonyManagerClass;
				Object ITelephonyStub;
				Class<?> ITelephonyClass;

				TelephonyManager telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);

				telephonyManagerClass = Class.forName(telephonyManager
						.getClass().getName());
				Method getITelephonyMethod = telephonyManagerClass
						.getDeclaredMethod("getITelephony");
				getITelephonyMethod.setAccessible(true);
				ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
				ITelephonyClass = Class.forName(ITelephonyStub.getClass()
						.getName());

				if (ON) {
					dataConnSwitchmethod = ITelephonyClass
							.getDeclaredMethod("enableDataConnectivity");
				} else {
					dataConnSwitchmethod = ITelephonyClass
							.getDeclaredMethod("disableDataConnectivity");
				}
				dataConnSwitchmethod.setAccessible(true);
				dataConnSwitchmethod.invoke(ITelephonyStub);

			} else {
				// on Gingerbread+
				final ConnectivityManager conman = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				final Class<?> conmanClass = Class.forName(conman.getClass()
						.getName());
				final Field iConnectivityManagerField = conmanClass
						.getDeclaredField("mService");
				iConnectivityManagerField.setAccessible(true);
				final Object iConnectivityManager = iConnectivityManagerField
						.get(conman);
				final Class<?> iConnectivityManagerClass = Class
						.forName(iConnectivityManager.getClass().getName());
				final Method setMobileDataEnabledMethod = iConnectivityManagerClass
						.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
				setMobileDataEnabledMethod.setAccessible(true);
				setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
			}
		} catch (Exception e) {
			if (Log.LOGV) {
				Log.v(t + ".turnOnDataConnection(): error while turning on/off data");
			}
			//e.printStackTrace();
		}

	}

}
