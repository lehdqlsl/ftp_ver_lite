package com.sotolab.ftplite;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import static com.sotolab.ftplite.Constants.FILE_NAME;
import static com.sotolab.ftplite.Constants.FILE_SIZE;

public class ListViewAdapters extends BaseAdapter {

	public ArrayList<HashMap<String, String>> list;
	Activity activity;
	TextView fileName;
	TextView fileSize;

	public ListViewAdapters(Activity activity,
			ArrayList<HashMap<String, String>> list) {
		// TODO Auto-generated constructor stub
		super();
		this.activity = activity;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub

		LayoutInflater inflater = activity.getLayoutInflater();

		if (arg1 == null) {
			arg1 = inflater.inflate(R.layout.down_item, null);
			fileName = (TextView) arg1.findViewById(R.id.name);
			fileSize = (TextView) arg1.findViewById(R.id.size);
		}

		HashMap<String, String> map = list.get(arg0);
		fileName.setText(map.get(FILE_NAME));
		fileSize.setText(map.get(FILE_SIZE));
		return arg1;
	}

}
