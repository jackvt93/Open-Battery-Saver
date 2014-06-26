/*
 * Copyright (C) 2011 University of Washington
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
package vn.cybersoft.obs.android.adapters;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

import vn.cybersoft.obs.android.activities.R;
import vn.cybersoft.obs.android.models.MainMenuItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Luan Vu
 *
 */
public class MainMenuItemAdapter extends ArrayAdapter<MainMenuItem> {

	private ImageView image;
	private TextView caption;
	//private Button caption;
	private List<MainMenuItem> items = new ArrayList<MainMenuItem>();

	/**
	 * @param context
	 * @param resource
	 */
	public MainMenuItemAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void add(MainMenuItem object) {
		items.add(object);
		super.add(object);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}
	
	@Override
	public MainMenuItem getItem(int position) {
		return items.get(position);
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		
		if(null == rowView) {
			LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.main_menu_list_row, parent, false);
		}
		
		caption = (TextView)rowView.findViewById(R.id.caption);
		//caption = (Button)rowView.findViewById(R.id.caption);
		caption.setText(getItem(position).caption);
		caption.setCompoundDrawablesWithIntrinsicBounds(getItem(position).imageRes, 0, 0, 0);
		return rowView;
	}
	
	
	
	
}
