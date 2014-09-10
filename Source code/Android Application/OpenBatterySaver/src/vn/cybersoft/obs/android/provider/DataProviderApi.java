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

package vn.cybersoft.obs.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The Api contains definitions for the supported URIs and data columns.
 * </p>
 * <ul>
 * <li>The {@link TimeSchedulesColumns} table holds the user created time schedules</li>
 * </ul>
 * 
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 */
public final class DataProviderApi {
    /**
     * This authority is used for writing to or querying from the clock
     * provider.
     */
	public final static String AUTHORITY = "vn.cybersoft.obs.android";
	
    /**
     * This utility class cannot be instantiated
     */
	private DataProviderApi() {};
	
    /**
     * Constants for the OptimalModes table, which contains the info mode for manual optimizing.
     */
    protected interface OptimalModesColumns extends BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/optimal_modes");
        
        /**
         * Name of optimal mode
         * <P>Type: STRING</P>
         */
        public static final String NAME = "name";

        /**
         * Description
         * <P>Type: STRING</P>
         */
        public static final String DESC = "desc";

        /**
         * true if this mode can edit
         * <P>Type: BOOLEAN</P>
         */
        public static final String CAN_EDIT = "canEdit";

        /**
         * Screen brightness range of 0 - 255
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN_BRIGHTNESS = "screenBrightness";

        /**
         * Screen time out in UTC milliseconds
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN_TIMEOUT = "screenTimeout";
        
        /**
         * Is vibrate on ?
         * <P>Type: BOOLEAN </P>
         */
        public static final String VIBRATE = "vibrate";
        
        /**
         * Is wifi on ?
         * <P>Type: BOOLEAN </P>
         */
        public static final String WIFI = "wifi";
        
        /**
         * Is bluetooth on ?
         * <P>Type: BOOLEAN </P>
         */
        public static final String BLUETOOTH = "bluetooth";
        
        /**
         * Is sync on ?
         * <P>Type: BOOLEAN </P>
         */
        public static final String SYNC = "sync";
        
        /**
         * Is haptic feedback on ?
         * <P>Type: BOOLEAN </P>
         */
        public static final String HAPTIC_FEEDBACK = "hapticFeedback";
    }
    
    /**
     * Constants for the TimeSchedules table, which contains the user created time schedules.
     */
    protected interface TimeSchedulesColumns extends BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/time_schedules");

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>Type: INTEGER</P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * Schedule time in UTC milliseconds from the epoch.
         * <P>Type: INTEGER</P>
         */
        public static final String SCHEDULE_TIME = "scheduletime";

        /**
         * True if schedule is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";
        
        /**
         * Mode id to change when time wake up
         * <P>Type: INTEGER </P>
         */
        public static final String MODE_ID = "modeid";
    }
    
    
	
}
