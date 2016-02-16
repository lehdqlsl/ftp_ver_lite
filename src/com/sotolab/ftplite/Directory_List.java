package com.sotolab.ftplite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Directory_List extends Activity implements
		AdapterView.OnItemClickListener, OnClickListener {

	private ListView lv;
	private Button btn;
	private List<String> item = null;
	private List<String> path = null;
	private File Directrory = Environment.getExternalStorageDirectory();
	private String root = Directrory.getPath();
	private TextView mPath;
	private String location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directory__list);

		lv = (ListView) findViewById(R.id.d_list);
		mPath = (TextView) findViewById(R.id.d_path);
		btn = (Button) findViewById(R.id.chose);

		getDir(root);

		lv.setOnItemClickListener(this);
		btn.setOnClickListener(this);
	}

	public void getDir(String dirPath) {

		mPath.setText("Location: " + dirPath);

		location = dirPath;

		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory() && !file.isHidden() && file.canRead()){
				item.add(file.getName());
				path.add(file.getPath());
			}
				
		}
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_activated_1, item));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO Auto-generated method stub
		final File file = new File(path.get(position));

		if (file.canRead())
			getDir(path.get(position));
		else {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.lite_48)
					.setTitle("[" + file.getName() + "] folder can't be read!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
								}
							}).show();
		}

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("path", location);
		setResult(RESULT_OK, intent);
		finish();
	}
}
