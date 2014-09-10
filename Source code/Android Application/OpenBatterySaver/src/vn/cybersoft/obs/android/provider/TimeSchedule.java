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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.utilities.Log;
import vn.cybersoft.obs.android.utilities.Utils;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.CursorLoader;
import android.text.format.DateFormat;

/**
 * @author Atom
 *
 */
public class TimeSchedule implements Parcelable, DataProviderApi.TimeSchedulesColumns {
    // This action triggers the ScheduleReceiver as well as the TimeScheduleRunner. It
    // is a public action used in the manifest for receiving TimeSchedule broadcasts
    // from the alarm manager.
    public static final String SCHEDULE_MODE_ACTION = "vn.cybersoft.obs.android.intent.action.SCHEDULE_MODE"; 
    
    // This extra is the raw TimeSchedule object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String INTENT_RAW_DATA = "intent.extra.schedule_raw";
    
    // This string is used when passing an schedules object through an intent.
    public static final String INTENT_EXTRA = "intent.extra.schedule";
	
    // This string is used to identify the schedule id passed to SetTimeSchedule from the
    // list of time schedules.
    public static final String EXTRA_ID = "time_schedule_id"; 
	
    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E k:mm";

    private final static String M12 = "h:mm aa";
    
    public final static String M24 = "kk:mm";
    
    /**
     * schedule start with an invalid id when it hasn't been saved to the database.
     */
    public static final long INVALID_ID = -1;
    
    // Used when filtering enabled schedules.
    public static final String WHERE_ENABLED = ENABLED + "=1";
	
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER =
            HOUR + ", " + MINUTES + " ASC";

    public static final String[] QUERY_COLUMNS = {
        _ID, HOUR, MINUTES, DAYS_OF_WEEK, SCHEDULE_TIME,
        ENABLED, MODE_ID };

    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    public static final int ID_INDEX = 0;
    public static final int HOUR_INDEX = 1;
    public static final int MINUTES_INDEX = 2;
    public static final int DAYS_OF_WEEK_INDEX = 3;
    public static final int SCHEDULE_TIME_INDEX = 4; 
    public static final int ENABLED_INDEX = 5;
    public static final int MODE_ID_INDEX = 6;
    
    private static final int COLUMN_COUNT = MODE_ID_INDEX + 1;
    
    private static ContentValues createContentValues(TimeSchedule schedule) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        // Set the schedule_time value if this schedule does not repeat. This will be
        // used later to disable expire schedules.
        long time = 0;
        if (!schedule.daysOfWeek.isRepeatSet()) {
            time = calculateTimeSchedule(schedule);
        }

        values.put(ENABLED, schedule.enabled ? 1 : 0);
        values.put(HOUR, schedule.hour);
        values.put(MINUTES, schedule.minutes);
        values.put(SCHEDULE_TIME, schedule.time);
        values.put(DAYS_OF_WEEK, schedule.daysOfWeek.getCoded());
        values.put(MODE_ID, schedule.modeId); 

        return values;
    }
    
    public static Uri getUri(int scheduleId) {
    	return ContentUris.withAppendedId(CONTENT_URI, scheduleId);
    }
    
    public static long getId(Uri contentUri) { 
    	return ContentUris.parseId(contentUri);
    }
    
    public static CursorLoader getSchedulesCursorLoader(Context context) {
        return new CursorLoader(context, CONTENT_URI, QUERY_COLUMNS, null, null, DEFAULT_SORT_ORDER);
    }
	
    /**
     * Creates a new time schedule and fills in the given schedule's id.
     */
    public static long addTimeSchedule(Context context, TimeSchedule schedule) {
        ContentValues values = createContentValues(schedule);
        Uri uri = context.getContentResolver().insert(
                CONTENT_URI, values);
        schedule.id = (int) ContentUris.parseId(uri);
        setNextAction(context); 
        return calculateTimeSchedule(schedule);
    }
	
    public static void deleteTimeSchedule(Context context, int scheduleId) {
        if (scheduleId == -1) return;

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, scheduleId);
        contentResolver.delete(uri, "", null);
        
        setNextAction(context); 
    }

    /**
     * Queries all time schedules
     * @return cursor over all schedules
     */
    public static Cursor getTimeSchedulesCursor(ContentResolver contentResolver) {
        return contentResolver.query(
                CONTENT_URI, QUERY_COLUMNS,
                null, null, DEFAULT_SORT_ORDER);
    }

    // Private method to get a more limited set of schedules from the database.
    private static Cursor getFilteredTimeSchedulesCursor(ContentResolver contentResolver) {
        return contentResolver.query(CONTENT_URI,
        		QUERY_COLUMNS, WHERE_ENABLED,
                null, null);
    }
    
    /**
     * Return an TimeSchedule object representing the schedule id in the database.
     * Returns null if no schedule exists.
     */
    public static TimeSchedule getTimeSchedule(ContentResolver contentResolver, int scheduleId) {
        Cursor cursor = contentResolver.query(
        		ContentUris.withAppendedId(CONTENT_URI, scheduleId),
                QUERY_COLUMNS,
                null, null, null);
        TimeSchedule schedule = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
            	schedule = new TimeSchedule(cursor);
            }
            cursor.close();
        }
        return schedule;
    }
    
    /**
     * A convenience method to set an time schedule in the Time Schedule
     * content provider.
     * @return Time when the time schedule will fire.
     */
    public static long setTimeSchedule(Context context, TimeSchedule timeSchedule) {
        ContentValues values = createContentValues(timeSchedule);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(
                ContentUris.withAppendedId(CONTENT_URI, timeSchedule.id),
                values, null, null);
        setNextAction(context);
        return calculateTimeSchedule(timeSchedule);
    }

    /**
     * A convenience method to enable or disable an time schedule.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     */

    public static void enableTimeSchedule(
            final Context context, final int id, boolean enabled) {
    	enableTimeScheduleInternal(context, id, enabled);
    	setNextAction(context);
    }

    private static void enableTimeScheduleInternal(final Context context,
            final int id, boolean enabled) {
    	enableTimeScheduleInternal(context, getTimeSchedule(context.getContentResolver(), id),
                enabled);
    }

    private static void enableTimeScheduleInternal(final Context context,
            final TimeSchedule schedule, boolean enabled) {
        if (schedule == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(ENABLED, enabled ? 1 : 0);

        // If we are enabling the schedule, calculate schedule time since the time
        // value in TimeSchedule may be old.
        if (enabled) {
            long time = 0;
            if (!schedule.daysOfWeek.isRepeatSet()) {
                time = calculateTimeSchedule(schedule);
            }
            values.put(SCHEDULE_TIME, time);
        } 
        resolver.update(ContentUris.withAppendedId(
        		CONTENT_URI, schedule.id), values, null, null);
    }
    
    public static TimeSchedule calculateNextAction(final Context context) {
    	TimeSchedule schedule = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getFilteredTimeSchedulesCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                	TimeSchedule s = new TimeSchedule(cursor);
                	
                    if (s.time == 0) {
                        s.time = calculateTimeSchedule(s);
                    } else if (s.time < now) {
                        Log.v("Disabling expired schedule set for " + Log.formatTime(s.time));
                        // Expired schedule, disable it and move along.
                        enableTimeScheduleInternal(context, s, false);
                        continue;
                    }
                    if (s.time < minTime) {
                        minTime = s.time;
                        schedule = s;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return schedule;
    }
    
    /**
     * Disables non-repeating schedules that have passed.  Called at
     * boot.
     */
    public static void disableExpiredSchedules(final Context context) {
        Cursor cur = getFilteredTimeSchedulesCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        if (cur.moveToFirst()) {
            do {
                TimeSchedule schedule = new TimeSchedule(cur);
                // A time of 0 means this schedule repeats. If the time is
                // non-zero, check if the time is before now.
                if (schedule.time != 0 && schedule.time < now) {
                    Log.v("Disabling expired schedule set for " +
                          Log.formatTime(schedule.time));
                    enableTimeScheduleInternal(context, schedule, false);
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }

	/**
	 * Called at system startup, on time/timezone change, and whenever the user
	 * changes schedule settings
	 */
	public static void setNextAction(final Context context) {
		TimeSchedule schedule = calculateNextAction(context);
		if (schedule != null) {
			enableAction(context, schedule, schedule.time);
		} else {
			disableAction(context);
		}
	}
	
    /**
     * Sets action in AlarmManger.  This is what will
     * actually launch the action when the schedule triggers.
     *
     * @param schedule TimeSchedule.
     * @param atTimeInMillis milliseconds since epoch
     */
    @SuppressLint("NewApi") 
    private static void enableAction(Context context, final TimeSchedule schedule,
            final long atTimeInMillis) {

        if (Log.LOGV) {
            Log.v("** setSchedule id " + schedule.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(SCHEDULE_MODE_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the TimeSchedule class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The ScheduleReceiver class knows to build the TimeSchedule
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        schedule.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(INTENT_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context, schedule.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Utils.isKitKatOrLater()) {
        	am.setExact(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
		}

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
    }
    
    /**
     * @param id Schedule ID.
     * 
     */
    static void disableAction(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(SCHEDULE_MODE_ACTION),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
        saveNextAlarm(context, "");
    }
    
    private static long calculateTimeSchedule(TimeSchedule schedule) { 
        return calculateTimeSchedule(schedule.hour, schedule.minutes, schedule.daysOfWeek)
                .getTimeInMillis();
    }
	
    static Calendar calculateTimeSchedule(int hour, int minute,
            TimeSchedule.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if schedule is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextSchedule(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }
	
	public static String formatTime(final Context context, int hour, int minute,
			TimeSchedule.DaysOfWeek daysOfWeek) {
		Calendar c = calculateTimeSchedule(hour, minute, daysOfWeek);
		return formatTime(context, c);
	}

	public static String formatTime(final Context context, Calendar c) {
		String format = get24HourMode(context) ? M24 : M12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}
	
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    static void saveNextAlarm(final Context context, String timeString) {
        android.provider.Settings.System.putString(context.getContentResolver(),
                                  Settings.System.NEXT_ALARM_FORMATTED,
                                  timeString);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
    
	public static final Parcelable.Creator<TimeSchedule> CREATOR = new Parcelable.Creator<TimeSchedule>() {

		@Override
		public TimeSchedule createFromParcel(Parcel p) {
			return new TimeSchedule(p);
		}

		@Override
		public TimeSchedule[] newArray(int size) {
			return new TimeSchedule[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
		p.writeLong(modeId);
	}
	
	public int id;
	public boolean enabled;
	public int hour;
	public int minutes;
	public DaysOfWeek daysOfWeek;
	public long time;
	public long modeId;
	
	public TimeSchedule(Cursor c) {
		id = c.getInt(ID_INDEX);
		enabled = c.getInt(ENABLED_INDEX) >= 1;
		hour = c.getInt(HOUR_INDEX);
		minutes = c.getInt(MINUTES_INDEX);
		daysOfWeek = new DaysOfWeek(c.getInt(DAYS_OF_WEEK_INDEX));
		time = c.getLong(SCHEDULE_TIME_INDEX);
		modeId = c.getInt(MODE_ID_INDEX);
	}
	
    public TimeSchedule(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        modeId = p.readInt();
    }
	
	// Creates a default schedule at the current time.
	public TimeSchedule() {
		id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        daysOfWeek = new DaysOfWeek(0x7f);
        modeId = -1;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
        if (!(o instanceof TimeSchedule)) return false;
        final TimeSchedule other = (TimeSchedule) o;
        return id == other.id;
	}
	
	@Override
	public String toString() {
        return "TimeSchedule{" +
                ", id=" + id +
                ", enabled=" + enabled +
                ", hour=" + hour +
                ", minutes=" + minutes +
                ", daysOfWeek=" + daysOfWeek +
                ", time=" + time +
                ", modeId=" + modeId +
                '}';
	}
	
    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    public static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
        private int mDays;

        public DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ?
                        context.getText(R.string.never).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0) ret.append(
                            context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next schedule
         * @param c must be set to today
         */
        public int getNextSchedule(Calendar c) { 
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }

}
