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

import java.util.ArrayList;

import vn.cybersoft.obs.android.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
public class OptimizationFragment extends Fragment {
	private static final String t = OptimizationFragment.class.getSimpleName();
	
	public static final int LAYOUT_ID = R.layout.optimization_fragment;
	private static final String TAB_MODE = "tab_mode";
	private static final String TAB_SMART = "tab_smart";
	
	private static final String CURRENT_TAB = "currentTab";
	
	private FragmentTabHost mTabHost;
    //private ViewPager mViewPager;
    //private OptimizationPagerAdapter mTabsAdapter;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//mTabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
		//mTabHost.setup();
		
		mTabHost = new FragmentTabHost(getActivity());
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.pager);
		
		//mViewPager = (ViewPager) mTabHost.findViewById(R.id.pager);
		
/*		mTabsAdapter = new OptimizationPagerAdapter(getActivity(), getChildFragmentManager(), mTabHost, mViewPager);
		
		mTabsAdapter.addTab(mTabHost.newTabSpec(TAB_MODE).setIndicator(getString(R.string.mode)),
				ModeTabFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec(TAB_SMART).setIndicator(getString(R.string.smart)),
				SmartTabFragment.class, null);*/
		
		mTabHost.addTab(mTabHost.newTabSpec(TAB_MODE).setIndicator(getString(R.string.mode)),
				ModeTabFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_SMART).setIndicator(getString(R.string.smart)),
				SmartTabFragment.class, null);

		for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
			TextView tabTitle = (TextView) mTabHost.getTabWidget()
					.getChildAt(i).findViewById(android.R.id.title);
			tabTitle.setTextSize(16f);
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_indicator_ab);
		}
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(CURRENT_TAB)) {
				mTabHost.setCurrentTabByTag(savedInstanceState.getString(CURRENT_TAB)); 
			}
		}
		
		return mTabHost;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENT_TAB, mTabHost.getCurrentTabTag()); 
	}
	
/*    public static class OptimizationPagerAdapter extends FragmentPagerAdapter 
    			implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

		private final TabHost mTabHost;
    	private final Context mContext;
    	private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }
    	
        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }
    	
        public OptimizationPagerAdapter(Context context, FragmentManager manager, TabHost tabHost, ViewPager pager) {
            super(manager);
            mContext = context;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }
        
        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }
    	

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

		@Override
		public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
		}
    	
    }*/
}
