package vn.cybersoft.obs.android.activities;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.R.layout;
import vn.cybersoft.obs.android.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ScheduleModeTimeActivity extends Activity {
	public static final int LAYOUT_ID = R.layout.schedule_mode_time_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT_ID);
	}

}
