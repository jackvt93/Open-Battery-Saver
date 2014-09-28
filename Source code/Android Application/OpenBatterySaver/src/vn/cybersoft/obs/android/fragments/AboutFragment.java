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

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.utilities.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * @author Luan Vu
 *
 */
public class AboutFragment extends Fragment {
	private static final String TAG = "AboutFragment";
	public static final int LAYOUT_ID = R.layout.about_fragment; 
	
	private WebView mWebView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(getString(R.string.app_name) + " > " + getString(R.string.about));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);
		// Load content
		mWebView = (WebView) view.findViewById(R.id.webview);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		String helpFileName="about_en.html";
		/*if (Preferences.getInstance().getLanguage().equals("en"))
		helpFileName="about_us.html";
		else
		helpFileName="about_us_vi.html";*/
		String html = Utils.getFileContent(inflater.getContext(), helpFileName);
		mWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
}
