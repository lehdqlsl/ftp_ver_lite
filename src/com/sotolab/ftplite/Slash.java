package com.sotolab.ftplite;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

public class Slash extends Activity {

	TextView txt_ver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//overridePendingTransition(R.anim.slide_out_left,R.anim.fade);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_slash);

		txt_ver = (TextView)findViewById(R.id.version);
		
		Handler handler = new Handler();
		handler.postDelayed(new splashHandler(), 1350);
		PackageInfo pi = null;

		try {

		pi = getPackageManager().getPackageInfo(getPackageName(), 0);

		} catch (NameNotFoundException e) {

		// TODO Auto-generated catch block

		e.printStackTrace();

		}

		String verSion = pi.versionName;
		txt_ver.setText("Ver : "+verSion);
		
		/*
		View titleView = getWindow().findViewById(android.R.id.title);
		ViewParent parent = titleView.getParent();
		View parentView = (View) parent;
		parentView.setBackgroundDrawable(getResources().getDrawable(R.drawable.lite_96));
		*/
	}

	private class splashHandler implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startActivity(new Intent(getApplication(), FTPmain.class));
			overridePendingTransition(R.anim.slide_in_right,R.anim.fade);
			Slash.this.finish();
		}
	}
}
