package vn.cybersoft.obs.android.fragments;

import vn.cybersoft.obs.android.activities.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConsumptionFragment extends Fragment {
	private static final String TAG = "ConsumptionFragment";

	
	public static final int LAYOUT_ID = R.layout.consumption_fragment; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(LAYOUT_ID, container, false);
		
		return view;
	}
}
