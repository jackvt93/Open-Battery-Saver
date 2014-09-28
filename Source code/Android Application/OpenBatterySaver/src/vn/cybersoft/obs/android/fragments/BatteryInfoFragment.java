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

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.activities.CleanerActivity;
import vn.cybersoft.obs.android.models.BatteryInfo;
import vn.cybersoft.obs.android.provider.BatteryTrace;
import vn.cybersoft.obs.android.utilities.DeviceUtils;
import vn.cybersoft.obs.android.utilities.Log;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Luan Vu
 *
 */
public class BatteryInfoFragment extends Fragment {
	private static final String t = BatteryInfoFragment.class.getSimpleName();
	public static final int LAYOUT_ID = R.layout.battery_info_fragment; 
	
	private static final int LIMIT_DATE_SHOW = 2;
	private static final int MINIMUM_VALUE_TO_SHOW_CHART = 4;
	
	private IncomeinHandler mHandler = new IncomeinHandler(this); 
	
	private BatteryInfo mBatteryInfo;
	private BatteryInfoReceiver mBatteryInfoReceiver;
	
	private TextView mTemperature, mVoltage, mCapacity, mTimeLeftHour, mTimeLeftMin, mBatteryLevel;
	private TextView mTimeLeftText;
	private ImageView mBatteryImg;
	
	private Button mCleanApp;
	
	
	private ViewGroup mUsageChart;
	
	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer;
	private GraphicalView mChartView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(getString(R.string.app_name) + " > " + getString(R.string.battery_info));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);
		
		mTimeLeftText = (TextView) view.findViewById(R.id.time_left);
		mTimeLeftHour = (TextView) view.findViewById(R.id.time_left_hour);
		mTimeLeftMin = (TextView) view.findViewById(R.id.time_left_minutes);
		
		mTemperature = (TextView) view.findViewById(R.id.temperature_text);
		mVoltage = (TextView) view.findViewById(R.id.voltage_text);
		mBatteryLevel = (TextView) view.findViewById(R.id.battery_level_text);
		mBatteryImg = (ImageView) view.findViewById(R.id.battery_img);
		
		mCapacity = (TextView) view.findViewById(R.id.capacity_text);
		mCapacity.setText(getBatteryCapacity()+ ""); 
		
		mBatteryInfo = new BatteryInfo();
		mBatteryInfoReceiver = new BatteryInfoReceiver();
		
		mCleanApp = (Button) view.findViewById(R.id.cleanUpButton);
		mCleanApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanApp();
			}
		});
		
		drawChart();
		mUsageChart = (ViewGroup) view.findViewById(R.id.battery_usage_chart);
		
		if (mChartView != null) { 
			mUsageChart.addView(mChartView, 0);
		} else {
			mUsageChart.findViewById(R.id.empty_usage).setVisibility(View.VISIBLE); 
		}
		
		return view;
	}
	
	private void cleanApp() {
        new AlertDialog.Builder(getActivity())
        	.setTitle(getString(R.string.clean_up))
        	.setMessage(getString(R.string.clean_up_confirm))
        	.setNegativeButton(android.R.string.cancel, null)
        	.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                    	Intent i = new Intent(getActivity(), CleanerActivity.class);
                    	startActivity(i);
                    }
                }).show();
	}
	
	private void drawChart() {
		mRenderer = new XYMultipleSeriesRenderer();
		mDataset = new XYMultipleSeriesDataset();
		
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(5);
		renderer.setColor(Color.parseColor("#0099cc")); 
		renderer.setDisplayBoundingPoints(true);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(10);
        //FillOutsideLine fillOutsideLine = new FillOutsideLine(Type.BOUNDS_ALL);
        //fillOutsideLine.setColor(Color.parseColor("#0099cc")); 
        //renderer.addFillOutsideLine(fillOutsideLine); 
		
        //mRenderer.setChartTitle("Battery Level (%)");
        mRenderer.setChartTitleTextSize(30f);
        mRenderer.setLabelsTextSize(20f);
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
		mRenderer.setPanEnabled(true, false);
		mRenderer.setZoomEnabled(false, false);
        mRenderer.setYAxisMax(DeviceUtils.getBatteryScale(getActivity()));
        mRenderer.setYAxisMin(0);
        //mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.GRAY);
        mRenderer.setClickEnabled(false);
        
        
        TimeSeries series = new TimeSeries("");
        
  /*      long value = new Date().getTime() - 3 * TimeChart.DAY;
        for (int i = 0; i < 5; i++) {
			series.add(new Date(value + i * TimeChart.DAY / 4), i);
		}*/
        
        List<BatteryTrace> traces = BatteryTrace.getClosestTraceData(getActivity().getContentResolver(), LIMIT_DATE_SHOW);
        
        boolean chartVisible = traces.size() >= MINIMUM_VALUE_TO_SHOW_CHART;
        
        if (chartVisible) {
            Calendar panMinimumX = Calendar.getInstance();
            panMinimumX.set(Calendar.HOUR_OF_DAY, traces.get(0).hour);
            panMinimumX.set(Calendar.MINUTE, traces.get(0).minutes);
            
            Calendar panMaximumX = Calendar.getInstance();
            panMaximumX.set(Calendar.HOUR_OF_DAY, traces.get(traces.size()-1).hour); 
            panMaximumX.set(Calendar.MINUTE, traces.get(traces.size()-1).minutes); 
            //mRenderer.setPanLimits(new double[] {panMinimumX.getTimeInMillis(), panMaximumX.getTimeInMillis() * 2, 0, 0});
            for (BatteryTrace b : traces) {
            	//System.out.println(b.toString()); 
            	Calendar c = Calendar.getInstance();
            	c.set(Calendar.HOUR_OF_DAY, b.hour);
            	c.set(Calendar.MINUTE, b.minutes);
            	series.add(c.getTime(), b.level);
    		}
            mDataset.addSeries(series); 
            mChartView = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, "hh:mm aa");
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.v(t + ".onStart()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(t + ".onResume()");
		getActivity().registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(t + ".onPause()");
		getActivity().unregisterReceiver(mBatteryInfoReceiver); 
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.v(t + ".onStop()");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(t + ".onDestroyView()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(t + ".onDestroy()");
	}
	
	private void updateLayout() {
		mTemperature.setText((float)mBatteryInfo.temperature / 10 + "");
		mVoltage.setText((float)mBatteryInfo.voltage / 1000 + "");
		
		int batteryPercentage = mBatteryInfo.getBatteryPercentage();
		boolean isCharging = mBatteryInfo.status == BatteryManager.BATTERY_STATUS_CHARGING;
		mBatteryLevel.setText(batteryPercentage + "%");  
		
		if (isCharging) {
			mTimeLeftText.setText(getString(R.string.charging_time_left));
			if (batteryPercentage <= 10) {
				mBatteryImg.setImageResource(R.drawable.psac0); 
			} else if(batteryPercentage <= 25) {
				mBatteryImg.setImageResource(R.drawable.psac1);
			} else if(batteryPercentage <= 50) {
				mBatteryImg.setImageResource(R.drawable.psac2);
			} else if(batteryPercentage <= 75) {
				mBatteryImg.setImageResource(R.drawable.psac3);
			} else {
				mBatteryImg.setImageResource(R.drawable.psac4);
			}
		} else {
			mTimeLeftText.setText(getString(R.string.time_left)); 
			if(batteryPercentage <= 10) {
				mBatteryImg.setImageResource(R.drawable.p1);
			} else if(batteryPercentage <= 25) {
				mBatteryImg.setImageResource(R.drawable.p2);
			} else if(batteryPercentage <= 50) {
				mBatteryImg.setImageResource(R.drawable.p3);
			} else if(batteryPercentage <= 75) {
				mBatteryImg.setImageResource(R.drawable.p4);
			} else {
				mBatteryImg.setImageResource(R.drawable.p5);
			}
		}
	}
	
	private class BatteryInfoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Log.LOGV) { 
				Log.v(t + ".BatteryInfoReceiver.onReceive()"); 
			}
			mBatteryInfo.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
			mBatteryInfo.heath = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
			mBatteryInfo.present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
			mBatteryInfo.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			mBatteryInfo.scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			mBatteryInfo.iconSmall = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
			mBatteryInfo.plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			mBatteryInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			mBatteryInfo.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
			mBatteryInfo.technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
			mBatteryInfo.invalidCharger = intent.getIntExtra("invalid_charger", 0);
			mHandler.sendEmptyMessage(0);
		}
		
	}
	
	/*
	 * Used to prevent memory leaks
	 */
	static class IncomeinHandler extends Handler {
		private final WeakReference<BatteryInfoFragment> mTarget;
		
		IncomeinHandler(BatteryInfoFragment target) {
			mTarget = new WeakReference<BatteryInfoFragment>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			BatteryInfoFragment target = mTarget.get();
			if (target != null) {
				target.updateLayout();
			}
		}
	}
	
	private double getBatteryCapacity() {
		Object powerProfile = null;
		final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
		
		try {
			powerProfile = Class.forName(POWER_PROFILE_CLASS)
					.getConstructor(android.content.Context.class).newInstance(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double ret = 0;
		try {
			ret = (Double) Class.forName(POWER_PROFILE_CLASS)
					.getMethod("getBatteryCapacity")
					.invoke(powerProfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
}
