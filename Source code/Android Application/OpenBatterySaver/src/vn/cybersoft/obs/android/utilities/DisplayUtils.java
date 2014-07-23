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
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class DisplayUtils {
	public static Notification createNotification(RemoteViews content, int smallIcon) {
		return new NotificationCompat.Builder(OBS.getInstance().getApplicationContext())
							.setSmallIcon(smallIcon) 
							.setContent(content)
							.build();
	}
	
	
	public static void createDialog() {
		
	}
	
	public static void createToast() {
		
	}
}
