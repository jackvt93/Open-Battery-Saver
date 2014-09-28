package vn.cybersoft.obs.android.batterystats;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

//import com.android.internal.os.PowerProfile;
//import com.android.settings.HelpUtils;






import java.lang.reflect.InvocationTargetException;
import java.util.List;

import vn.cybersoft.obs.android.R;

/**
 * Displays a list of apps and subsystems that consume power, ordered by how much power was
 * consumed since the last time it was unplugged.
 */
public class PowerUsageSummary extends PreferenceFragment {
	
    /**
     * Include all of the data in the stats, including previously saved data.
     */
    public static final int STATS_SINCE_CHARGED = 0;
	

    private static final boolean DEBUG = false;

    private static final String TAG = "PowerUsageSummary";

    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_BATTERY_STATUS = "battery_status";

    private static final int MENU_STATS_TYPE = Menu.FIRST;
    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
    private static final int MENU_HELP = Menu.FIRST + 2;

    private PreferenceGroup mAppListGroup;
    private Preference mBatteryStatusPref;

    private int mStatsType = STATS_SINCE_CHARGED;

    private static final int MIN_POWER_THRESHOLD = 5;
    private static final int MAX_ITEMS_TO_LIST = 10;

    private BatteryStatsHelper mStatsHelper;

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                String batteryLevel = vn.cybersoft.obs.android.utilities.Utils.getBatteryPercentage(intent);
                String batteryStatus = vn.cybersoft.obs.android.utilities.Utils.getBatteryStatus(getResources(),
                        intent);
                String batterySummary = context.getResources().getString(R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
                mBatteryStatusPref.setTitle(batterySummary);
                mStatsHelper.clearStats();
                try {
					refreshStats();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStatsHelper = new BatteryStatsHelper(activity, mHandler);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
			mStatsHelper.create(icicle);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        addPreferencesFromResource(R.xml.power_usage_summary);
        mAppListGroup = (PreferenceGroup) findPreference(KEY_APP_LIST);
        mBatteryStatusPref = mAppListGroup.findPreference(KEY_BATTERY_STATUS);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatteryInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        try {
			refreshStats();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void onPause() {
        mStatsHelper.pause();
        mHandler.removeMessages(BatteryStatsHelper.MSG_UPDATE_NAME_ICON);
        getActivity().unregisterReceiver(mBatteryInfoReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStatsHelper.destroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		return false;
/*        if (preference instanceof BatteryHistoryPreference) {
            Parcel hist = Parcel.obtain();
            mStatsHelper.getStats().writeToParcelWithoutUids(hist, 0);
            byte[] histData = hist.marshall();
            Bundle args = new Bundle();
            args.putByteArray(BatteryHistoryDetail.EXTRA_STATS, histData);
            PreferenceActivity pa = (PreferenceActivity)getActivity();
            pa.startPreferencePanel(BatteryHistoryDetail.class.getName(), args,
                    R.string.history_details_title, null, null, 0);
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        if (!(preference instanceof PowerGaugePreference)) {
            return false;
        }
        PowerGaugePreference pgp = (PowerGaugePreference) preference;
        BatterySipper sipper = pgp.getInfo();
        mStatsHelper.startBatteryDetailPage((PreferenceActivity) getActivity(), sipper, true);
        return super.onPreferenceTreeClick(preferenceScreen, preference);*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
/*        if (DEBUG) {
            menu.add(0, MENU_STATS_TYPE, 0, R.string.menu_stats_total)
                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
                    .setAlphabeticShortcut('t');
        }
        MenuItem refresh = menu.add(0, MENU_STATS_REFRESH, 0, R.string.menu_stats_refresh)
                .setIcon(R.drawable.ic_menu_refresh_holo_dark)
                .setAlphabeticShortcut('r');
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        String helpUrl;
        if (!TextUtils.isEmpty(helpUrl = getResources().getString(R.string.help_url_battery))) {
            final MenuItem help = menu.add(0, MENU_HELP, 0, R.string.help_label);
            HelpUtils.prepareHelpMenuItem(getActivity(), help, helpUrl);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		return false; 
/*        switch (item.getItemId()) {
            case MENU_STATS_TYPE:
                if (mStatsType == BatteryStats.STATS_SINCE_CHARGED) {
                    mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
                } else {
                    mStatsType = BatteryStats.STATS_SINCE_CHARGED;
                }
                refreshStats();
                return true;
            case MENU_STATS_REFRESH:
                mStatsHelper.clearStats();
                refreshStats();
                return true;
            default:
                return false;
        }*/
    }

    private void addNotAvailableMessage() {
        Preference notAvailable = new Preference(getActivity());
        notAvailable.setTitle(R.string.power_usage_not_available);
        mAppListGroup.addPreference(notAvailable);
    }

    private void refreshStats() throws Exception {
        mAppListGroup.removeAll();
        mAppListGroup.setOrderingAsAdded(false);

        mBatteryStatusPref.setOrder(-2);
        mAppListGroup.addPreference(mBatteryStatusPref);
       /* BatteryHistoryPreference hist = new BatteryHistoryPreference(
                getActivity(), mStatsHelper.getStats());*/
        //hist.setOrder(-1);
        //mAppListGroup.addPreference(hist);

        //if (mStatsHelper.getPowerProfile().getAveragePower(PowerProfile.POWER_SCREEN_FULL) < 10) {
        
        double averagePower = (Double) mStatsHelper
        					.getPowerProfile()
			        		.getClass()
			        		.getMethod("getAveragePower", String.class)
			        		.invoke(mStatsHelper.getPowerProfile(), "screen.full");
        
        if (averagePower < 10) {
            addNotAvailableMessage();
            return;
        }
        mStatsHelper.refreshStats(false);
        List<BatterySipper> usageList = mStatsHelper.getUsageList();
        for (BatterySipper sipper : usageList) {
            if (sipper.getSortValue() < MIN_POWER_THRESHOLD) continue;
            final double percentOfTotal =
                    ((sipper.getSortValue() / mStatsHelper.getTotalPower()) * 100);
            if (percentOfTotal < 1) continue;
            PowerGaugePreference pref =
                    new PowerGaugePreference(getActivity(), sipper.getIcon(), sipper);
            final double percentOfMax =
                    (sipper.getSortValue() * 100) / mStatsHelper.getMaxPower();
            sipper.percent = percentOfTotal;
            pref.setTitle(sipper.name);
            pref.setOrder(Integer.MAX_VALUE - (int) sipper.getSortValue()); // Invert the order
            pref.setPercent(percentOfMax, percentOfTotal);
            if (sipper.uidObj != null) {
                //pref.setKey(Integer.toString(sipper.uidObj.getUid()));
            	int uid = (Integer) sipper.uidObj.getClass().getMethod("getUid").invoke(null);
            	pref.setKey(Integer.toString(uid));
            }
            mAppListGroup.addPreference(pref);
            if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST+1)) break;
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BatteryStatsHelper.MSG_UPDATE_NAME_ICON:
                    BatterySipper bs = (BatterySipper) msg.obj;
                    //PowerGaugePreference pgp = (PowerGaugePreference) findPreference(Integer.toString(bs.uidObj.getUid()));
				int uid = 0;
				try {
					uid = (Integer) bs.uidObj.getClass().getMethod("getUid").invoke(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace(); 
				}
                    PowerGaugePreference pgp = (PowerGaugePreference) findPreference(Integer.toString(uid));
                    if (pgp != null) {
                        pgp.setAppIcon(bs.icon);
                        pgp.setTitle(bs.name);
                    }
                    break;
                case BatteryStatsHelper.MSG_REPORT_FULLY_DRAWN:
                    Activity activity = getActivity();
                    if (activity != null) {
                        //activity.reportFullyDrawn();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
