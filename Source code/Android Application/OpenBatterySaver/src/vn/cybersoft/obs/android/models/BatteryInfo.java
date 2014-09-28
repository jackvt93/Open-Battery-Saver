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

package vn.cybersoft.obs.android.models;

import android.content.Intent;
import android.os.BatteryManager;

/**
 * this class contain info of battery received on battery broadcast receiver
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class BatteryInfo {
	public int status;
	public int heath;
	public boolean present;
	public int level;
	public int scale;
	public int iconSmall;
	public int plugged;
	public int voltage;
	public int temperature;
	public String technology;
	public int invalidCharger;
	
	/**
	 * 
	 */
	public BatteryInfo() {
	}


	public BatteryInfo(int status, int heath, boolean present, int level,
			int scale, int iconSmall, int plugged, int voltage,
			int temperature, String technology, int invalidCharger) {
		this.status = status;
		this.heath = heath;
		this.present = present;
		this.level = level;
		this.scale = scale;
		this.iconSmall = iconSmall;
		this.plugged = plugged;
		this.voltage = voltage;
		this.temperature = temperature;
		this.technology = technology;
		this.invalidCharger = invalidCharger;
	}
	
    public int getBatteryPercentage() {
        return level * 100 / scale;
    }
	
	
}
