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

package vn.cybersoft.obs.android.activities;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import vn.cybersoft.obs.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

/**
 * @author Atom
 *
 */
public class MessageBoxActivity extends SherlockFragmentActivity { 
	public static final int LAYOUT_ID = R.layout.message_box_layout;
	
	private String mTitle;
	private String mMessage;
	
	public MessageBoxActivity() {
		
	}
	
/*	public MessageBoxActivity(String title, String message) {
		mTitle = title;
		mMessage = message;
	}*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		showAsPopup(); 
		setContentView(LAYOUT_ID);
		
		ActionBar actionBar = getSupportActionBar();
		
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.message_actionbar_layout); 
		TextView titleText = (TextView) actionBar.getCustomView().findViewById(R.id.text1);
		titleText.setText(getIntent().getExtras().getString("title")); 
	}
	
	private void showAsPopup() {
		this.requestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = this.getWindow().getAttributes(); 
	    params.height = LayoutParams.WRAP_CONTENT; 
	    params.width = LayoutParams.MATCH_PARENT;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    this.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
	}
}
