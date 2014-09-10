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

package vn.cybersoft.obs.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class BatteryProgress extends View {

	public BatteryProgress(Context context) {
		this(context, null);
	}
	
	public BatteryProgress(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BatteryProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	
	public BatteryProgress(Context context, AttributeSet attrs, int defStyle, int styleRes) {
		super(context, attrs, defStyle);
	}
	
}
