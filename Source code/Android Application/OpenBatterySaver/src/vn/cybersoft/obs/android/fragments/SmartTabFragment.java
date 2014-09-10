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
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.SwitchPreference;
import android.support.v4.preference.PreferenceFragment;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class SmartTabFragment extends PreferenceFragment {
	public static final String KEY_SMART_OPTIMAZATION_ENABLE = "smart_optimization_enable";
	public static final String KEY_AUTO_CLEAR_APP_SCREEN_LOCK = "auto_clear_apps_screen_lock";
	
	private SwitchPreference mSmartOptimizationEnablePreference;
	private CheckBoxPreference mAutoClearAppPreference;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.smart_optimization_prefs);
        
        mAutoClearAppPreference = (CheckBoxPreference) findPreference(KEY_AUTO_CLEAR_APP_SCREEN_LOCK);
        
    }

	

}
