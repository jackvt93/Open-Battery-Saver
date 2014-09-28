/*
Copyright (C) 2011 The University of Michigan

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Please send inquiries to powertutor@umich.edu
 */

package vn.cybersoft.obs.andriod.batterystats2.phone;

import vn.cybersoft.obs.andriod.batterystats2.components.Audio.AudioData;
import vn.cybersoft.obs.andriod.batterystats2.components.CPU.CpuData;
import vn.cybersoft.obs.andriod.batterystats2.components.GPS.GpsData;
import vn.cybersoft.obs.andriod.batterystats2.components.LCD.LcdData;
import vn.cybersoft.obs.andriod.batterystats2.components.OLED.OledData;
import vn.cybersoft.obs.andriod.batterystats2.components.Sensors.SensorData;
import vn.cybersoft.obs.andriod.batterystats2.components.Threeg.ThreegData;
import vn.cybersoft.obs.andriod.batterystats2.components.Wifi.WifiData;

public interface PhonePowerCalculator {
	public double getLcdPower(LcdData data);

	public double getOledPower(OledData data);

	public double getCpuPower(CpuData data);

	public double getAudioPower(AudioData data);

	public double getGpsPower(GpsData data);

	public double getWifiPower(WifiData data);

	public double getThreeGPower(ThreegData data);

	public double getSensorPower(SensorData data);
}

