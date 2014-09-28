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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import vn.cybersoft.obs.android.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class Utils {
	
    /**
     * Returns whether the SDK is KitKat or later
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
    
    /**
     * Returns whether the SDK is Honey comb or later
     */
    public static boolean isHoneyCombOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    public static boolean isIceCreamSandwichOrLater() {
    	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    /**
     * Get string from resId which format by string. If it not found in class, return resId string
     * 
     * @param context
     * @param resIdStr
     * @param c
     * @return
     */
    public static String getString(Context context, String resIdStr, Class<?> c) {
		int resId = ReflectionUtils.getResourceId(resIdStr, c);
    	return resId != -1 ? context.getString(resId) : resIdStr;
    }
    
    public static boolean saveToPreference(Context context, String key, Object value) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = sharedPreferences.edit();
    	
    	if (value instanceof String) {
			editor.putString(key, (String) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Long) {
			editor.putLong(key, (Long) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else {
			// insert code here for handle errror
		}
    	
    	return editor.commit();
    }
    
    public static boolean removeValueFromPreference(Context context, String key) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = sharedPreferences.edit();
    	editor.remove(key);
    	return editor.commit();
    }
    
    public static Object getValueFromPreference(Context context, Class<?> type, String key, Object defValue) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	
    	Object ret = null;
    	
    	if (String.class.isAssignableFrom(type)) {
			ret = sharedPreferences.getString(key, (String) defValue);
		} else if (Integer.class.isAssignableFrom(type)) {
    		ret = sharedPreferences.getInt(key, (Integer) defValue);
		} else if (Long.class.isAssignableFrom(type)) {
			ret = sharedPreferences.getLong(key, (Long) defValue);
		} else if (Float.class.isAssignableFrom(type)) {
			ret = sharedPreferences.getFloat(key, (Float) defValue);
		} else if (Boolean.class.isAssignableFrom(type)) {
			ret = sharedPreferences.getBoolean(key, (Boolean) defValue);
		}
    	
    	return ret;
	}
    
    public static String getBatteryPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryChangedIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return String.valueOf(level * 100 / scale) + "%";
    }
    
    public static String getBatteryStatus(Resources res, Intent batteryChangedIntent) {
        final Intent intent = batteryChangedIntent;
        int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        String statusString;
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            statusString = res.getString(R.string.battery_info_status_charging);
            if (plugType > 0) {
                int resId;
                if (plugType == BatteryManager.BATTERY_PLUGGED_AC) {
                    resId = R.string.battery_info_status_charging_ac;
                } else if (plugType == BatteryManager.BATTERY_PLUGGED_USB) {
                    resId = R.string.battery_info_status_charging_usb;
                } else {
                    resId = R.string.battery_info_status_charging_wireless;
                }
                statusString = statusString + " " + res.getString(resId);
            }
        } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            statusString = res.getString(R.string.battery_info_status_discharging);
        } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            statusString = res.getString(R.string.battery_info_status_not_charging);
        } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
            statusString = res.getString(R.string.battery_info_status_full);
        } else {
            statusString = res.getString(R.string.battery_info_status_unknown);
        }
        return statusString;
    }
    
    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        //return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
        
        boolean ret = false; 
		try {
			ret = (Boolean) cm.getClass().getMethod("isNetworkSupported", Integer.class).invoke(null, ConnectivityManager.TYPE_MOBILE);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
        return (ret == false);
    }
    

    /**
     * Get file content by filename
     *
     * @param c
     * @param filename
     * @return content String
     */
    public static String getFileContent(Context c, String filename) {
    	try {
    		InputStream fin = c.getAssets().open(filename);
    		byte[] buffer = new byte[fin.available()];
    		fin.read(buffer);
    		fin.close();
    		return new String(buffer);
    	} catch (IOException e) {
    		Log.e(e.getLocalizedMessage());
    	}
    	
    	return "";
    }
    
}
