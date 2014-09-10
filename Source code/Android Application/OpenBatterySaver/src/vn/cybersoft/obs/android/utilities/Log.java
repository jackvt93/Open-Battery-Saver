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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class Log {
    public final static String LOGTAG = "OpenBatterySaver";

    public static final boolean LOGV = true;

    public static void d(String logMe) {
        android.util.Log.d(LOGTAG, logMe);
    }

    public static void v(String logMe) {
        android.util.Log.v(LOGTAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
    }

    public static void i(String logMe) {
        android.util.Log.i(LOGTAG, logMe);
    }

    public static void e(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    public static void e(String logMe, Exception ex) {
        android.util.Log.e(LOGTAG, logMe, ex);
    }

    public static void w(String logMe) {
        android.util.Log.w(LOGTAG, logMe);
    }

    public static void wtf(String logMe) {
        android.util.Log.wtf(LOGTAG, logMe);
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss.SSS/E").format(new Date(millis));
    }
}
