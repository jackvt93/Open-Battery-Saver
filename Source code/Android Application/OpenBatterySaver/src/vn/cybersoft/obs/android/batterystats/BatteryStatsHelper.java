package vn.cybersoft.obs.android.batterystats;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceActivity;
import android.telephony.SignalStrength;
import android.util.Log;
import android.util.SparseArray;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.application.OBS;
import vn.cybersoft.obs.android.batterystats.PowerUsageDetail.DrainType;
import vn.cybersoft.obs.android.utilities.ReflectionUtils;

/**
 * A helper class for retrieving the power usage information for all applications and services.
 *
 * The caller must initialize this class as soon as activity object is ready to use (for example, in
 * onAttach() for Fragment), call create() in onCreate() and call destroy() in onDestroy().
 */
@SuppressLint("NewApi") public class BatteryStatsHelper {
    /**
     * Include all of the data in the stats, including previously saved data.
     */
    public static final int STATS_SINCE_CHARGED = 0;
    
    public static final String SERVICE_NAME = "batterystats";

    private static final boolean DEBUG = false;

    private static final String TAG = BatteryStatsHelper.class.getSimpleName();

    //private static BatteryStatsImpl sStatsXfer;
    private static Object sStatsXfer;
    //private IBatteryStats mBatteryInfo;
    private Object mBatteryInfo;
    private UserManager mUm;
    //private BatteryStatsImpl mStats;
    private Object mStats;
    //private PowerProfile mPowerProfile;
    private Object mPowerProfile;

    private final List<BatterySipper> mUsageList = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mWifiSippers = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mBluetoothSippers = new ArrayList<BatterySipper>();
    private final SparseArray<List<BatterySipper>> mUserSippers
            = new SparseArray<List<BatterySipper>>();
    private final SparseArray<Double> mUserPower = new SparseArray<Double>();

    private int mStatsType = STATS_SINCE_CHARGED;

    private long mStatsPeriod = 0;
    private double mMaxPower = 1;
    private double mTotalPower;
    private double mWifiPower;
    private double mBluetoothPower;

    // How much the apps together have left WIFI running.
    private long mAppWifiRunning;

    /** Queue for fetching name and icon for an application */
    private ArrayList<BatterySipper> mRequestQueue = new ArrayList<BatterySipper>();

    private Activity mActivity;
    private Handler mHandler;

    private class NameAndIconLoader extends Thread {
        private boolean mAbort = false;

        public NameAndIconLoader() {
            super("BatteryUsage Icon Loader");
        }

        public void abort() {
            mAbort = true;
        }

        @Override
        public void run() {
            while (true) {
                BatterySipper bs;
                synchronized (mRequestQueue) {
                    if (mRequestQueue.isEmpty() || mAbort) {
                        mHandler.sendEmptyMessage(MSG_REPORT_FULLY_DRAWN);
                        return;
                    }
                    bs = mRequestQueue.remove(0);
                }
                try {
					bs.loadNameAndIcon();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }

    private NameAndIconLoader mRequestThread;

    public BatteryStatsHelper(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;
    }

    /** Clears the current stats and forces recreating for future use. */
    public void clearStats() {
        mStats = null;
    }

/*    public BatteryStatsImpl getStats() {
        if (mStats == null) {
            load();
        }
        return mStats;
    }*/
    
    public Object getStats() {
        if (mStats == null) {
            load();
        }
        return mStats;
    }

/*    public PowerProfile getPowerProfile() {
        return mPowerProfile;
    }*/
    
    public Object getPowerProfile() {
        return mPowerProfile;
    }

    public void create(Bundle icicle) throws Exception {
        if (icicle != null) {
            mStats = sStatsXfer;
        }
        
        IBinder batteryStatsService = (IBinder) ReflectionUtils
        		.getClassMethod("android.os.ServiceManager", "getService", String.class)
        		.invoke(null, "batterystats");
        
        Class<?> IBatteryStats = ReflectionUtils.getClass("com.android.internal.app.IBatteryStats$Stub");
        
        mBatteryInfo = IBatteryStats.getMethod("asInterface", IBinder.class).invoke(null, batteryStatsService);
        
        for (Method method : mBatteryInfo.getClass().getMethods()) {
			System.out.println(mBatteryInfo.getClass().getSimpleName() + " Method: " + method.getName()); 
		}
        
        //mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        mUm = (UserManager) mActivity.getSystemService(Context.USER_SERVICE);
        
        mPowerProfile = ReflectionUtils
        		.getClassConstructor("com.android.internal.os.PowerProfile", Context.class).newInstance(mActivity);
        
        for (Method m : mPowerProfile.getClass().getMethods()) {
        	System.out.print(mPowerProfile.getClass().getSimpleName() + " Method: " + m.getName() + "("); 
        	for (Class c : m.getParameterTypes()) {
        		System.out.print(c.getSimpleName());
        		System.out.print(", ");
			}
        	System.out.println(")");
		}
        //mPowerProfile = new PowerProfile(mActivity);
    }

    public void pause() {
        if (mRequestThread != null) {
            mRequestThread.abort();
        }
    }

    public void destroy() {
        if (mActivity.isChangingConfigurations()) {
            sStatsXfer = mStats;
        } else {
            BatterySipper.sUidCache.clear();
        }
    }

    public void startBatteryDetailPage(
            PreferenceActivity caller, BatterySipper sipper, boolean showLocationButton) {
        // Initialize mStats if necessary.
        /*getStats();

        Bundle args = new Bundle();
        args.putString(PowerUsageDetail.EXTRA_TITLE, sipper.name);
        args.putInt(PowerUsageDetail.EXTRA_PERCENT, (int)
                Math.ceil(sipper.getSortValue() * 100 / mTotalPower));
        args.putInt(PowerUsageDetail.EXTRA_GAUGE, (int)
                Math.ceil(sipper.getSortValue() * 100 / mMaxPower));
        args.putLong(PowerUsageDetail.EXTRA_USAGE_DURATION, mStatsPeriod);
        args.putString(PowerUsageDetail.EXTRA_ICON_PACKAGE, sipper.defaultPackageName);
        args.putInt(PowerUsageDetail.EXTRA_ICON_ID, sipper.iconId);
        args.putDouble(PowerUsageDetail.EXTRA_NO_COVERAGE, sipper.noCoveragePercent);
        if (sipper.uidObj != null) {
            //args.putInt(PowerUsageDetail.EXTRA_UID, sipper.uidObj.getUid());
            args.putInt(PowerUsageDetail.EXTRA_UID, (Integer) sipper.uidObj.getClass().getMethod("getUid").invoke(null));
        }
        args.putSerializable(PowerUsageDetail.EXTRA_DRAIN_TYPE, sipper.drainType);
        args.putBoolean(PowerUsageDetail.EXTRA_SHOW_LOCATION_BUTTON, showLocationButton);

        int[] types;
        double[] values;
        switch (sipper.drainType) {
            case APP:
            case USER:
            {
                //Uid uid = sipper.uidObj;
                Object uid = sipper.uidObj;
                types = new int[] {
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_gps,
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_data_recv,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_wifi_recv,
                    R.string.usage_type_data_wifi_send,
                    R.string.usage_type_audio,
                    R.string.usage_type_video,
                };
                values = new double[] {
                    sipper.cpuTime,
                    sipper.cpuFgTime,
                    sipper.wakeLockTime,
                    sipper.gpsTime,
                    sipper.wifiRunningTime,
                    sipper.mobileRxBytes,
                    sipper.mobileTxBytes,
                    sipper.wifiRxBytes,
                    sipper.wifiTxBytes,
                    0,
                    0
                };

                if (sipper.drainType == DrainType.APP) {
                    Writer result = new StringWriter();
                    //PrintWriter printWriter = new FastPrintWriter(result, false, 1024);
                    PrintWriter printWriter = (PrintWriter) ReflectionUtils
                    		.getClassConstructor("com.android.internal.util.FastPrintWriter", Writer.class, 
                    				Boolean.class, Integer.class).newInstance(result, false, 1024);
                    //mStats.dumpLocked(printWriter, "", mStatsType, uid.getUid());
                    mStats.getClass()
                    .getMethod("dumpLocked", PrintWriter.class, String.class, Integer.class, Integer.class)
                    .invoke(null, printWriter, "", mStatsType, uid.getClass().getMethod("getUid").invoke(null)); 
                    
                    printWriter.flush();
                    args.putString(PowerUsageDetail.EXTRA_REPORT_DETAILS, result.toString());

                    result = new StringWriter();
                    //printWriter = new FastPrintWriter(result, false, 1024);
                    printWriter = (PrintWriter) ReflectionUtils
                    		.getClassConstructor("com.android.internal.util.FastPrintWriter", Writer.class, 
                    				Boolean.class, Integer.class).newInstance(result, false, 1024);
                    //mStats.dumpCheckinLocked(printWriter, mStatsType, uid.getUid());
                    mStats.getClass()
                    .getMethod("dumpCheckinLocked", PrintWriter.class, Integer.class, Integer.class)
                    .invoke(null, printWriter, mStatsType, uid.getClass().getMethod("getUid").invoke(null)); 
                    printWriter.flush();
                    args.putString(PowerUsageDetail.EXTRA_REPORT_CHECKIN_DETAILS,
                            result.toString());
                }
            }
            break;
            case CELL:
            {
                types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_no_coverage
                };
                values = new double[] {
                    sipper.usageTime,
                    sipper.noCoveragePercent
                };
            }
            break;
            case WIFI:
            {
                types = new int[] {
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_recv,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_wifi_recv,
                    R.string.usage_type_data_wifi_send,
                };
                values = new double[] {
                    sipper.usageTime,
                    sipper.cpuTime,
                    sipper.cpuFgTime,
                    sipper.wakeLockTime,
                    sipper.mobileRxBytes,
                    sipper.mobileTxBytes,
                    sipper.wifiRxBytes,
                    sipper.wifiTxBytes,
                };
            } break;
            case BLUETOOTH:
            {
                types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_recv,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_wifi_recv,
                    R.string.usage_type_data_wifi_send,
                };
                values = new double[] {
                    sipper.usageTime,
                    sipper.cpuTime,
                    sipper.cpuFgTime,
                    sipper.wakeLockTime,
                    sipper.mobileRxBytes,
                    sipper.mobileTxBytes,
                    sipper.wifiRxBytes,
                    sipper.wifiTxBytes,
                };
            } break;
            default:
            {
                types = new int[] {
                    R.string.usage_type_on_time
                };
                values = new double[] {
                    sipper.usageTime
                };
            }
        }
        args.putIntArray(PowerUsageDetail.EXTRA_DETAIL_TYPES, types);
        args.putDoubleArray(PowerUsageDetail.EXTRA_DETAIL_VALUES, values);
        caller.startPreferencePanel(PowerUsageDetail.class.getName(), args,
                R.string.details_title, null, null, 0);*/
    }

    /**
     * Refreshes the power usage list.
     * @param includeZeroConsumption whether includes those applications which have consumed very
     *                               little power up till now.
     */
    public void refreshStats(boolean includeZeroConsumption) {
        // Initialize mStats if necessary.
        getStats();

        mMaxPower = 0;
        mTotalPower = 0;
        mWifiPower = 0;
        mBluetoothPower = 0;
        mAppWifiRunning = 0;

        mUsageList.clear();
        mWifiSippers.clear();
        mBluetoothSippers.clear();
        mUserSippers.clear();
        mUserPower.clear();

        try {
			processAppUsage(includeZeroConsumption);
			processMiscUsage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Collections.sort(mUsageList);

        if (mHandler != null) {
            synchronized (mRequestQueue) {
                if (!mRequestQueue.isEmpty()) {
                    if (mRequestThread != null) {
                        mRequestThread.abort();
                    }
                    mRequestThread = new NameAndIconLoader();
                    mRequestThread.setPriority(Thread.MIN_PRIORITY);
                    mRequestThread.start();
                    mRequestQueue.notify();
                }
            }
        }
    }

    private void processAppUsage(boolean includeZeroConsumption) throws Exception {
        SensorManager sensorManager = (SensorManager) mActivity.getSystemService(
                Context.SENSOR_SERVICE);
        final int which = mStatsType;
        //final int speedSteps = mPowerProfile.getNumSpeedSteps();
        final int speedSteps = (Integer) mPowerProfile.getClass().getMethod("getNumSpeedSteps").invoke(mPowerProfile);
        
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            //powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
        	powerCpuNormal[p] = (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class, int.class).invoke(mPowerProfile, "cpu.active", p);
        }
        final double mobilePowerPerByte = getMobilePowerPerByte();
        final double wifiPowerPerByte = getWifiPowerPerByte();
        //long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which);
        long uSecTime = (Long) mStats.getClass().getMethod("computeBatteryRealtime", Long.class, Integer.class).invoke(mStats, SystemClock.elapsedRealtime() * 1000, which);
        long appWakelockTime = 0;
        BatterySipper osApp = null;
        mStatsPeriod = uSecTime;
        //SparseArray<? extends Uid> uidStats = mStats.getUidStats();
        SparseArray<? extends Object> uidStats = (SparseArray<? extends Object>) mStats.getClass().getMethod("getUidStats").invoke(mStats);
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            //Uid u = uidStats.valueAt(iu);
            Object u = uidStats.valueAt(iu);
            double p; // in mAs
            double power = 0; // in mAs
            double highestDrain = 0;
            String packageWithHighestDrain = null;
            //mUsageList.add(new AppUsage(u.getUid(), new double[] {power}));
            //Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            Map<String, ?> processStats = (Map<String, ?>) u.getClass().getMethod("getProcessStats").invoke(u);
            
            long cpuTime = 0;
            long cpuFgTime = 0;
            long wakelockTime = 0;
            long gpsTime = 0;
            if (DEBUG) Log.i(TAG, "UID " + u.getClass().getMethod("getUid").invoke(u));
            if (processStats.size() > 0) {
                // Process CPU time
                //for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats.entrySet()) {
            	for (Map.Entry<String, ?> ent : processStats.entrySet()) {
                    //Uid.Proc ps = ent.getValue();
                    Object ps = ent.getValue();
                    //final long userTime = ps.getUserTime(which);
                    final long userTime = (Long) ps.getClass().getMethod("getUserTime", Integer.class).invoke(ps, which);
                    //final long systemTime = ps.getSystemTime(which);
                    final long systemTime = (Long) ps.getClass().getMethod("getSystemTime", Integer.class).invoke(ps, which);
                    //final long foregroundTime = ps.getForegroundTime(which);
                    final long foregroundTime = (Long) ps.getClass().getMethod("getForegroundTime", Integer.class).invoke(ps, which);
                    cpuFgTime += foregroundTime * 10; // convert to millis
                    final long tmpCpuTime = (userTime + systemTime) * 10; // convert to millis
                    int totalTimeAtSpeeds = 0;
                    // Get the total first
                    for (int step = 0; step < speedSteps; step++) {
                        //cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                    	cpuSpeedStepTimes[step] = (Long) ps.getClass()
                    			.getMethod("getTimeAtCpuSpeedStep", Integer.class, Integer.class)
                    			.invoke(ps, step, which);
                        totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                    }
                    if (totalTimeAtSpeeds == 0) totalTimeAtSpeeds = 1;
                    // Then compute the ratio of time spent at each speed
                    double processPower = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        double ratio = (double) cpuSpeedStepTimes[step] / totalTimeAtSpeeds;
                        processPower += ratio * tmpCpuTime * powerCpuNormal[step];
                    }
                    cpuTime += tmpCpuTime;
                    if (DEBUG && processPower != 0) {
                        Log.i(TAG, String.format("process %s, cpu power=%.2f",
                                ent.getKey(), processPower / 1000));
                    }
                    power += processPower;
                    if (packageWithHighestDrain == null
                            || packageWithHighestDrain.startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    } else if (highestDrain < processPower
                            && !ent.getKey().startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    }
                }
            }
            if (cpuFgTime > cpuTime) {
                if (DEBUG && cpuFgTime > cpuTime + 10000) {
                    Log.i(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                }
                cpuTime = cpuFgTime; // Statistics may not have been gathered yet.
            }
            power /= 1000;
            if (DEBUG && power != 0) Log.i(TAG, String.format("total cpu power=%.2f", power));

            // Process wake lock usage
            //Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u.getWakelockStats();
            Map<String, ?> wakelockStats = (Map<String, ?>) u.getClass().getMethod("getWakelockStats").invoke(null);
            //for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : wakelockStats.entrySet()) {
            for (Map.Entry<String, ?> wakelockEntry : wakelockStats.entrySet()) {
                //Uid.Wakelock wakelock = wakelockEntry.getValue();
                Object wakelock = wakelockEntry.getValue();
                // Only care about partial wake locks since full wake locks
                // are canceled when the user turns the screen off.
                //BatteryStats.Timer timer = wakelock.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
                Object timer = wakelock.getClass().getMethod("getWakeTime", Integer.class).invoke(null, 0);
                if (timer != null) {
                    //wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
                    wakelockTime += (Long) timer.getClass().getMethod("getTotalTimeLocked", Long.class, Integer.class).invoke(null, uSecTime, which);
                }
            }
            wakelockTime /= 1000; // convert to millis
            appWakelockTime += wakelockTime;

            // Add cost of holding a wake lock
            //p = (wakelockTime * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
            p = (wakelockTime * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "cpu.awake")) / 1000;
            power += p;
            if (DEBUG && p != 0) Log.i(TAG, String.format("wakelock power=%.2f", p));

            // Add cost of mobile traffic
            //final long mobileRx = u.getNetworkActivityCount(NETWORK_MOBILE_RX_BYTES, mStatsType);
            final long mobileRx = (Long) u.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(null, 0, mStatsType);
            //final long mobileTx = u.getNetworkActivityCount(NETWORK_MOBILE_TX_BYTES, mStatsType);
            final long mobileTx = (Long) u.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(null, 1, mStatsType);
            p = (mobileRx + mobileTx) * mobilePowerPerByte;
            power += p;
            if (DEBUG && p != 0) Log.i(TAG, String.format("mobile power=%.2f", p));

            // Add cost of wifi traffic
            //final long wifiRx = u.getNetworkActivityCount(NETWORK_WIFI_RX_BYTES, mStatsType);
            final long wifiRx = (Long) u.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(null, 2, mStatsType);
            //final long wifiTx = u.getNetworkActivityCount(NETWORK_WIFI_TX_BYTES, mStatsType);
            final long wifiTx = (Long) u.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(null, 3, mStatsType);
            p = (wifiRx + wifiTx) * wifiPowerPerByte;
            power += p;
            if (DEBUG && p != 0) Log.i(TAG, String.format("wifi power=%.2f", p));

            // Add cost of keeping WIFI running.
            //long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;
            long wifiRunningTimeMs = (Long) u.getClass().getMethod("getWifiRunningTime", Long.class, Integer.class).invoke(null, uSecTime, which) / 1000;
            mAppWifiRunning += wifiRunningTimeMs;
            //p = (wifiRunningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
            p = (wifiRunningTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "wifi.on")) / 1000;
            power += p;
            if (DEBUG && p != 0) Log.i(TAG, String.format("wifi running power=%.2f", p));

            // Add cost of WIFI scans
            //long wifiScanTimeMs = u.getWifiScanTime(uSecTime, which) / 1000;
            long wifiScanTimeMs = (Long) u.getClass().getMethod("getWifiScanTime", Long.class, Integer.class).invoke(null, uSecTime, which) / 1000;
            //p = (wifiScanTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_SCAN)) / 1000;
            p = (wifiScanTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "wifi.scan")) / 1000;
            power += p;
            if (DEBUG && p != 0) Log.i(TAG, String.format("wifi scanning power=%.2f", p));
            //for (int bin = 0; bin < BatteryStats.Uid.NUM_WIFI_BATCHED_SCAN_BINS; bin++) {
            for (int bin = 0; bin < 5; bin++) {
                //long batchScanTimeMs = u.getWifiBatchedScanTime(bin, uSecTime, which) / 1000;
                long batchScanTimeMs = (Long) u.getClass().getMethod("getWifiBatchedScanTime", Integer.class, Long.class, Integer.class).invoke(null, bin, uSecTime, which) / 1000;
                //p = (batchScanTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_BATCHED_SCAN, bin));
                p = (batchScanTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class, Integer.class).invoke(null, "wifi.batchedscan", bin));
                power += p;
                if (DEBUG && p != 0) {
                    Log.i(TAG, String.format("wifi batched scanning lvl %d = %.2f", bin, p));
                }
            }

            // Process Sensor usage
            //Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u.getSensorStats();
            Map<Integer, ?> sensorStats = (Map<Integer, ?>) u.getClass().getMethod("getSensorStats").invoke(null);
            //for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry : sensorStats.entrySet()) {
            for (Map.Entry<Integer, ?> sensorEntry : sensorStats.entrySet()) {
                //Uid.Sensor sensor = sensorEntry.getValue();
                Object sensor = sensorEntry.getValue();
                int sensorHandle = (Integer) sensor.getClass().getMethod("getHandle").invoke(null);
                //BatteryStats.Timer timer = sensor.getSensorTime();
                Object timer = sensor.getClass().getMethod("getSensorTime").invoke(null);
                //long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
                long sensorTime = (Long) timer.getClass().getMethod("getTotalTimeLocked", Long.class, Integer.class).invoke(null, uSecTime, which) / 1000;
                double multiplier = 0;
                switch (sensorHandle) {
                    //case Uid.Sensor.GPS:
                    case -10000:
                        //multiplier = mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                        multiplier = (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "gps.on");
                        gpsTime = sensorTime;
                        break;
                    default:
                        List<Sensor> sensorList = sensorManager.getSensorList(
                                android.hardware.Sensor.TYPE_ALL);
                        for (android.hardware.Sensor s : sensorList) {
                            //if (s.getHandle() == sensorHandle) {
                        	int handle = (Integer) s.getClass().getMethod("getHandle").invoke(null);
                            if (handle == sensorHandle) {
                                multiplier = s.getPower();
                                break;
                            }
                        }
                }
                p = (multiplier * sensorTime) / 1000;
                power += p;
                if (DEBUG && p != 0) {
                    Log.i(TAG, String.format("sensor %s power=%.2f", sensor.toString(), p));
                }
            }

            if (DEBUG) Log.i(TAG, String.format("UID %d total power=%.2f", (Integer) u.getClass().getMethod("getUid").invoke(null), power));

            // Add the app to the list if it is consuming power
            boolean isOtherUser = false;
            //final int userId = UserHandle.getUserId(u.getUid());
            final int userId = (Integer) UserHandle.class.getMethod("getUserId", Integer.class).invoke(null, (Integer) u.getClass().getMethod("getUid").invoke(null));
            if (power != 0 || includeZeroConsumption || (Integer) u.getClass().getMethod("getUid").invoke(null) == 0) {
                BatterySipper app = new BatterySipper(mActivity, mRequestQueue, mHandler,
                        packageWithHighestDrain, DrainType.APP, 0, u,
                        new double[] {power});
                app.cpuTime = cpuTime;
                app.gpsTime = gpsTime;
                app.wifiRunningTime = wifiRunningTimeMs;
                app.cpuFgTime = cpuFgTime;
                app.wakeLockTime = wakelockTime;
                app.mobileRxBytes = mobileRx;
                app.mobileTxBytes = mobileTx;
                app.wifiRxBytes = wifiRx;
                app.wifiTxBytes = wifiTx;
                //if ((Integer) u.getClass().getMethod("getUid").invoke(null) == Process.WIFI_UID) {
                if ((Integer) u.getClass().getMethod("getUid").invoke(null) == 1010) {
                    mWifiSippers.add(app);
                //} else if ((Integer) u.getClass().getMethod("getUid").invoke(null) == Process.BLUETOOTH_UID) {
                } else if ((Integer) u.getClass().getMethod("getUid").invoke(null) == 1002) {
                    mBluetoothSippers.add(app);
                //} else if (userId != UserHandle.myUserId()
            } else if (userId != (Integer) UserHandle.class.getMethod("myUserId").invoke(null)
                        //&& UserHandle.getAppId((Integer) u.getClass().getMethod("getUid").invoke(null)) >= Process.FIRST_APPLICATION_UID) {
            	&& (Integer) UserHandle.class.getMethod("getAppId", Integer.class).invoke(null, (Integer) u.getClass().getMethod("getUid").invoke(null)) >= Process.FIRST_APPLICATION_UID) {
                    isOtherUser = true;
                    List<BatterySipper> list = mUserSippers.get(userId);
                    if (list == null) {
                        list = new ArrayList<BatterySipper>();
                        mUserSippers.put(userId, list);
                    }
                    list.add(app);
                } else {
                    mUsageList.add(app);
                }
                if ((Integer) u.getClass().getMethod("getUid").invoke(null) == 0) {
                    osApp = app;
                }
            }
            if (power != 0 || includeZeroConsumption) {
                //if ((Integer) u.getClass().getMethod("getUid").invoke(null) == Process.WIFI_UID) {
                if ((Integer) u.getClass().getMethod("getUid").invoke(null) == 1010) {
                    mWifiPower += power;
                //} else if ((Integer) u.getClass().getMethod("getUid").invoke(null) == Process.BLUETOOTH_UID) {
                } else if ((Integer) u.getClass().getMethod("getUid").invoke(null) == 1002) {
                    mBluetoothPower += power;
                } else if (isOtherUser) {
                    Double userPower = mUserPower.get(userId);
                    if (userPower == null) {
                        userPower = power;
                    } else {
                        userPower += power;
                    }
                    mUserPower.put(userId, userPower);
                } else {
                    if (power > mMaxPower) mMaxPower = power;
                    mTotalPower += power;
                }
            }
        }

        // The device has probably been awake for longer than the screen on
        // time and application wake lock time would account for.  Assign
        // this remainder to the OS, if possible.
        if (osApp != null) {
            //long wakeTimeMillis = mStats.computeBatteryUptime(SystemClock.uptimeMillis() * 1000, which) / 1000;
            long wakeTimeMillis = (Long) mStats.getClass().getMethod("computeBatteryUptime", Long.class, Integer.class).invoke(null, SystemClock.uptimeMillis() * 1000, which) / 1000;
            //wakeTimeMillis -= appWakelockTime + (mStats.getScreenOnTime(SystemClock.elapsedRealtime(), which) / 1000);
            wakeTimeMillis -= appWakelockTime + ((Long) mStats.getClass().getMethod("getScreenOnTime", Long.class, Integer.class).invoke(null, SystemClock.elapsedRealtime(), which) / 1000);
            if (wakeTimeMillis > 0) {
                //double power = (wakeTimeMillis * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
                double power = (wakeTimeMillis * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "cpu.awake")) / 1000;
                if (DEBUG) Log.i(TAG, "OS wakeLockTime " + wakeTimeMillis + " power " + power);
                osApp.wakeLockTime += wakeTimeMillis;
                osApp.value += power;
                osApp.values[0] += power;
                if (osApp.value > mMaxPower) mMaxPower = osApp.value;
                mTotalPower += power;
            }
        }
    }

    private void addPhoneUsage(long uSecNow) throws Exception {
        //long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
        long phoneOnTimeMs = (Long) mStats.getClass().getMethod("getPhoneOnTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        //double phoneOnPower = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) * phoneOnTimeMs / 1000;
        double phoneOnPower = (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "radio.active") * phoneOnTimeMs / 1000;
        addEntry(mActivity.getString(R.string.power_phone), DrainType.PHONE, phoneOnTimeMs,
                R.drawable.ic_settings_voice_calls, phoneOnPower);
    }

    private void addScreenUsage(long uSecNow) throws Exception {
        double power = 0;
        long screenOnTimeMs = (Long) mStats.getClass().getMethod("getScreenOnTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        power += screenOnTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "screen.on");
        final double screenFullPower =
                (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "screen.full");
        for (int i = 0; i < 5; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f) / 5;
                    /// BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
            //long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow, mStatsType) / 1000;
            long brightnessTime = (Long) mStats.getClass().getMethod("getScreenBrightnessTime", Integer.class, Long.class, Integer.class).invoke(null, i, uSecNow, mStatsType) / 1000;
            power += screenBinPower * brightnessTime;
            if (DEBUG) {
                Log.i(TAG, "Screen bin power = " + (int) screenBinPower + ", time = "
                        + brightnessTime);
            }
        }
        power /= 1000; // To seconds
        addEntry(mActivity.getString(R.string.power_screen), DrainType.SCREEN, screenOnTimeMs,
                R.drawable.ic_settings_display, power);
    }

    private void addRadioUsage(long uSecNow) throws Exception {
        double power = 0;
        //final int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
        final int BINS = 5;
        long signalTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            //long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow, mStatsType) / 1000;
            long strengthTimeMs = (Long) mStats.getClass().getMethod("getPhoneSignalStrengthTime", Integer.class, Long.class, Integer.class).invoke(null, i, uSecNow, mStatsType) / 1000;
            power += strengthTimeMs / 1000
                    * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class, Integer.class).invoke(null, "radio.on", i);
            signalTimeMs += strengthTimeMs;
        }
        //long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow, mStatsType) / 1000;
        long scanningTimeMs = (Long) mStats.getClass().getMethod("getPhoneSignalScanningTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        power += scanningTimeMs / 1000 * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null,
                "radio.scanning");
        BatterySipper bs =
                addEntry(mActivity.getString(R.string.power_cell), DrainType.CELL,
                        signalTimeMs, R.drawable.ic_settings_cell_standby, power);
        if (signalTimeMs != 0) {
            //bs.noCoveragePercent = mStats.getPhoneSignalStrengthTime(0, uSecNow, mStatsType)
            bs.noCoveragePercent = (Long) mStats.getClass().getMethod("getPhoneSignalStrengthTime", Integer.class, Long.class, Integer.class).invoke(null, 0, uSecNow, mStatsType)
                    / 1000 * 100.0 / signalTimeMs;
        }
    }

    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from, String tag) {
        for (int i=0; i<from.size(); i++) {
            BatterySipper wbs = from.get(i);
            if (DEBUG) Log.i(TAG, tag + " adding sipper " + wbs + ": cpu=" + wbs.cpuTime);
            bs.cpuTime += wbs.cpuTime;
            bs.gpsTime += wbs.gpsTime;
            bs.wifiRunningTime += wbs.wifiRunningTime;
            bs.cpuFgTime += wbs.cpuFgTime;
            bs.wakeLockTime += wbs.wakeLockTime;
            bs.mobileRxBytes += wbs.mobileRxBytes;
            bs.mobileTxBytes += wbs.mobileTxBytes;
            bs.wifiRxBytes += wbs.wifiRxBytes;
            bs.wifiTxBytes += wbs.wifiTxBytes;
        }
    }

    private void addWiFiUsage(long uSecNow) throws Exception {
        //long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
        long onTimeMs = (Long) mStats.getClass().getMethod("getWifiOnTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        //long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow, mStatsType) / 1000;
        long runningTimeMs = (Long) mStats.getClass().getMethod("getGlobalWifiRunningTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        if (DEBUG) Log.i(TAG, "WIFI runningTime=" + runningTimeMs
                + " app runningTime=" + mAppWifiRunning);
        runningTimeMs -= mAppWifiRunning;
        if (runningTimeMs < 0) runningTimeMs = 0;
        double wifiPower = (onTimeMs * 0 /* TODO */
                //* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)
                * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "wifi.on")
            + runningTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "wifi.on")) / 1000;
        if (DEBUG) Log.i(TAG, "WIFI power=" + wifiPower + " from procs=" + mWifiPower);
        BatterySipper bs = addEntry(mActivity.getString(R.string.power_wifi), DrainType.WIFI,
                runningTimeMs, R.drawable.ic_settings_wifi, wifiPower + mWifiPower);
        aggregateSippers(bs, mWifiSippers, "WIFI");
    }

    private void addIdleUsage(long uSecNow) throws Exception {
        //long idleTimeMs = (uSecNow - mStats.getScreenOnTime(uSecNow, mStatsType)) / 1000;
        long idleTimeMs = (uSecNow - (Long) mStats.getClass().getMethod("getScreenOnTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "cpu.idle"))
                / 1000;
        addEntry(mActivity.getString(R.string.power_idle), DrainType.IDLE, idleTimeMs,
                R.drawable.ic_settings_phone_idle, idlePower);
    }

    private void addBluetoothUsage(long uSecNow) throws Exception {
        //long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
        long btOnTimeMs = (Long) mStats.getClass().getMethod("getBluetoothOnTime", Long.class, Integer.class).invoke(null, uSecNow, mStatsType) / 1000;
        //double btPower = btOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON) / 1000;
        double btPower = btOnTimeMs * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "bluetooth.on") / 1000;
        //int btPingCount = mStats.getBluetoothPingCount();
        int btPingCount = (Integer) mStats.getClass().getMethod("getBluetoothPingCount").invoke(null);
        btPower += (btPingCount * (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "bluetooth.at")) / 1000;
        BatterySipper bs = addEntry(mActivity.getString(R.string.power_bluetooth),
                DrainType.BLUETOOTH, btOnTimeMs, R.drawable.ic_settings_bluetooth,
                btPower + mBluetoothPower);
        aggregateSippers(bs, mBluetoothSippers, "Bluetooth");
    }

    private void addUserUsage() throws Exception {
        for (int i=0; i<mUserSippers.size(); i++) {
            final int userId = mUserSippers.keyAt(i);
            final List<BatterySipper> sippers = mUserSippers.valueAt(i);
            //UserInfo info = mUm.getUserInfo(userId);
            Object info = mUm.getClass().getMethod("getUserInfo", Integer.class).invoke(null, userId);
            Drawable icon = null; 
            String name;
            if (info != null) {
                //icon = UserUtils.getUserIcon(mActivity, mUm, info, mActivity.getResources());
                //name = info != null ? info.name : null;
                name = info != null ? info.getClass().getField("name").toString() : null;
                if (name == null) {
                    //name = Integer.toString(info.id);
                    name = Integer.toString(info.getClass().getDeclaredField("name").getInt(info)); 
                }
                name = mActivity.getResources().getString(
                        R.string.running_process_item_user_label, name);
            } else {
                icon = null;
                name = mActivity.getResources().getString(
                        R.string.running_process_item_removed_user_label);
            }
            Double userPower = mUserPower.get(userId);
            double power = (userPower != null) ? userPower : 0.0;
            BatterySipper bs = addEntry(name, DrainType.USER, 0, 0, power);
            bs.icon = icon;
            aggregateSippers(bs, sippers, "User");
        }
    }

    /**
     * Return estimated power (in mAs) of sending a byte with the mobile radio.
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    private double getMobilePowerPerByte() throws Exception {
        final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from system
        //final double MOBILE_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;
        final double MOBILE_POWER = (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(mPowerProfile, "radio.active") / 3600;

        //final long mobileRx = mStats.getNetworkActivityCount(NETWORK_MOBILE_RX_BYTES, mStatsType);
        final long mobileRx = (Long) mStats.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(mStats, 0, mStatsType);
        //final long mobileTx = mStats.getNetworkActivityCount(NETWORK_MOBILE_TX_BYTES, mStatsType);
        final long mobileTx = (Long) mStats.getClass().getMethod("getNetworkActivityCount", Integer.class, Integer.class).invoke(mStats, 1, mStatsType);
        final long mobileData = mobileRx + mobileTx;

        final long radioDataUptimeMs = (Long) mStats.getClass().getMethod("getRadioDataUptime").invoke(null) / 1000;
        final long mobileBps = radioDataUptimeMs != 0
                ? mobileData * 8 * 1000 / radioDataUptimeMs
                : MOBILE_BPS;

        return MOBILE_POWER / (mobileBps / 8);
    }

    /**
     * Return estimated power (in mAs) of sending a byte with the Wi-Fi radio.
     */
    private double getWifiPowerPerByte() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from system
        //final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
        double WIFI_POWER = 0; 
		try {
			WIFI_POWER = (Double) mPowerProfile.getClass().getMethod("getAveragePower", String.class).invoke(null, "wifi.active") / 3600;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return WIFI_POWER / (WIFI_BPS / 8);
    }

    private void processMiscUsage() throws Exception {
        final int which = mStatsType;
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        final long uSecNow = (Long) mStats.getClass().getMethod("computeBatteryRealtime", Long.class, Integer.class).invoke(null, uSecTime, which);
        final long timeSinceUnplugged = uSecNow;
        if (DEBUG) {
            Log.i(TAG, "Uptime since last unplugged = " + (timeSinceUnplugged / 1000));
        }

        addUserUsage();
        addPhoneUsage(uSecNow);
        addScreenUsage(uSecNow);
        addWiFiUsage(uSecNow);
        addBluetoothUsage(uSecNow);
        addIdleUsage(uSecNow); // Not including cellular idle power
        // Don't compute radio usage if it's a wifi-only device
        //if (!com.android.settings.Utils.isWifiOnly(mActivity)) {
        if (!vn.cybersoft.obs.android.utilities.Utils.isWifiOnly(mActivity)) {
            addRadioUsage(uSecNow);
        }
    }

    private BatterySipper addEntry(String label, DrainType drainType, long time, int iconId,
            double power) {
        if (power > mMaxPower) mMaxPower = power;
        mTotalPower += power;
        BatterySipper bs = new BatterySipper(mActivity, mRequestQueue, mHandler,
                label, drainType, iconId, null, new double[] {power});
        bs.usageTime = time;
        bs.iconId = iconId;
        mUsageList.add(bs);
        return bs;
    }

    public List<BatterySipper> getUsageList() {
        return mUsageList;
    }

    static final int MSG_UPDATE_NAME_ICON = 1;
    static final int MSG_REPORT_FULLY_DRAWN = 2;

    public double getMaxPower() {
        return mMaxPower;
    }

    public double getTotalPower() {
        return mTotalPower;
    }

    private void load() {
        try {
        	//byte[] data = (byte[]) mBatteryInfo.getClass().getDeclaredMethod("getStatistics").invoke(mBatteryInfo);
        	Method getStatistics = mBatteryInfo.getClass().getDeclaredMethod("getStatistics");
        	
        	
        	Field[] fs = mBatteryInfo.getClass().getDeclaringClass().getDeclaredFields();
        	System.out.println(mBatteryInfo.getClass().getSimpleName());
        	System.out.println(mBatteryInfo.getClass().getDeclaringClass().getSimpleName());
        	
        	for (Field field : fs) {
        		field.setAccessible(true);
			}
        	
        	getStatistics.setAccessible(true);
        	byte[] data = null;
			try {
				data = (byte[]) getStatistics.invoke(mBatteryInfo); 
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof RemoteException) {
					e.printStackTrace();
				} else {
					e.printStackTrace();
				}
			}
            //byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            /*mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);*/
            Field CREATOR = ReflectionUtils.getClassField("com.android.internal.os.BatteryStatsImpl", "CREATOR");
            mStats = CREATOR.get(CREATOR).getClass().getMethod("createFromParcel", Parcelable.class).invoke(CREATOR, parcel);
            //mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
            mStats.getClass().getMethod("distributeWorkLocked", Integer.class).invoke(mStats, STATS_SINCE_CHARGED);
        } catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
