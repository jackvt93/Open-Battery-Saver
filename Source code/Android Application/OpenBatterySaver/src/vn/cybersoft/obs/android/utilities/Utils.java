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

import vn.cybersoft.obs.android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.TextView;

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
     * Returns whether the SDK is KitKat or later
     */
    public static boolean isHoneyCombOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB;
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
    
}
