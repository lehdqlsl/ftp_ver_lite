package com.sotolab.ftplite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FTPmain extends Activity {

	private Button _btn_pc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_btn_pc = (Button) findViewById(R.id.btn_pc);

		_btn_pc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "PC <---> Mobile Ελ½Ε",
						Toast.LENGTH_SHORT).show();
				Intent MainActivity = new Intent(FTPmain.this,
						MainActivity.class);

				startActivity(MainActivity);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		/*
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "TEST1");
		menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "TEST2");
		menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "TEST3");
		menu.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, "TEST4");
		*/
		return true;
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
