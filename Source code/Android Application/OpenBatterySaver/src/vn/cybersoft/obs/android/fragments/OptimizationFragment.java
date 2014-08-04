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
package vn.cybersoft.obs.android.fragments;

import vn.cybersoft.obs.android.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Luan Vu
 *
 */
public class OptimizationFragment extends Fragment {
	private static final String TAG = "OptimizationFragment";

	public static final int LAYOUT_ID = R.layout.optimization_fragment;
	private static final float TEXT_SIZE = 16f;
	
	private FragmentTabHost mTabHost;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);

		mTabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(),getChildFragmentManager(), R.id.realtabcontent);		        
		
		mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.mode)).setIndicator(getString(R.string.mode)),
				OptimalModeFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.smart)).setIndicator(getString(R.string.smart)),
				OptimalSmartFragment.class, null);

		for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
			TextView tabTitle = (TextView) mTabHost.getTabWidget()
					.getChildAt(i).findViewById(android.R.id.title);
			tabTitle.setTextSize(TEXT_SIZE);
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_indicator_ab);
		}
		
		return view;
	}
}
