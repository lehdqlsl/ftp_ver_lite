package com.sotolab.ftplite;

import static com.sotolab.ftplite.Constants.FILE_NAME;
import static com.sotolab.ftplite.Constants.FILE_SIZE;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.swiftp.CmdAbstractStore;
import org.swiftp.Defaults;
import org.swiftp.Globals;
import org.swiftp.MyLog;
import org.swiftp.TcpListener;
import org.swiftp.UiUpdater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button _startStop_Button = null;
	private TextView _ip_TextView = null;
	private ImageView _ftpStatus_imageView = null;
	private ListView _file_listView = null;
	private TextView _directory = null;
	private final int CALLER_REQUEST = 1;
	protected File workingDir = Globals.getChrootDir();
	protected DialogCreate dialogCreate;
	protected DialogDismiss dialogDismiss;

	private ProgressDialog progressDialog;

	ListViewAdapters adapter;
	private ArrayList<HashMap<String, String>> List;
	HashMap<String, String> temp;

	private String filename;
	protected MyLog myLog = new MyLog(this.getClass().getName());

	protected Context activityContext = this;

	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // We are being told to do a UI update
				// If more than one UI update is queued up, we only need to do
				// one.
				removeMessages(0);
				updateUi();
				break;
			case 1: // We are being told to display an error message
				removeMessages(1);
				break;
			case 2:
				onCreateDialog();
				break;
			case 3:
				handler.post(dialogCreate);
				break;
			case 4:
				handler.post(dialogDismiss);
				break;
			}

			String ip = msg.getData().getString("IP");
			if (ip != null) {
				Changebg(ip);
			}
		}
	};

	@SuppressLint("HandlerLeak")
	public Handler listhandler = new Handler() {
		public void handleMessage(Message msg) {
			filename = msg.getData().getString("data");

			String[] list = filename.split(":");

			temp = new HashMap<String, String>();

			temp.put(FILE_NAME, list[0]);
			temp.put(FILE_SIZE, list[1]);
			filename = list[0];

			List.add(temp);

			adapter = new ListViewAdapters(MainActivity.this, List);
			_file_listView.setAdapter(adapter);

			handler.sendEmptyMessage(2);
		}
	};

	public MainActivity() {

	}

	public class DialogCreate implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("클라이언트에서 업로드 중 . . .");
			progressDialog.setCancelable(false);
			progressDialog.show();

		}

	}

	public class DialogDismiss implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Toast.makeText(MainActivity.this, "업로드 완료", Toast.LENGTH_SHORT)
					.show();
		}

	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Request no title bar on our window
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Set the application-wide context global, if not already set

		Context myContext = Globals.getContext();
		if (myContext == null) {
			myContext = getApplicationContext();
			if (myContext == null) {
				throw new NullPointerException("Null context!?!?!?");
			}
			Globals.setContext(myContext);
		}

		// Inflate our UI from its XML layout description.
		setContentView(R.layout.main_activity);
		myLog.e("_startStop_Button");
		_ip_TextView = (TextView) findViewById(R.id.ip_address);
		_ftpStatus_imageView = (ImageView) findViewById(R.id.ftp_status);
		_file_listView = (ListView) findViewById(R.id.listView);
		_startStop_Button = (Button) findViewById(R.id.start_stop_button);
		_startStop_Button.setOnClickListener(startStopListener);
		List = new ArrayList<HashMap<String, String>>();
		_directory = (TextView) findViewById(R.id.directory);
		dialogCreate = new DialogCreate();
		dialogDismiss = new DialogDismiss();
		_startStop_Button.setBackgroundDrawable(getResources()
				.getDrawable(R.drawable.btn_begin));
		_startStop_Button.setTextColor(0xFFFFFFFF);
		
	}

	/**
	 * Whenever we regain focus, we should update the button text depending on
	 * the state of the server service.
	 */
	protected void onStart() {
		super.onStart();
		CmdAbstractStore.ListmsgsHandle(listhandler, handler);
		UiUpdater.registerClient(handler);
		TcpListener.UIhandler(handler);
		updateUi();
	}

	protected void onResume() {
		super.onResume();
		// If the required preferences are not present, launch the configuration
		// Activity.

		configSetting();
		if (!requiredSettingsDefined()) {
			launchCONFIG_KEYS();
		}
		UiUpdater.registerClient(handler);
		updateUi();
		// Register to receive wifi status broadcasts
		myLog.l(Log.DEBUG, "Registered for wifi updates");
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));
	}

	/*
	 * Whenever we lose focus, we must unregister from UI update messages from
	 * the FTPServerService, because we may be deallocated.
	 */
	protected void onPause() {
		super.onPause();
		UiUpdater.unregisterClient(handler);
		myLog.l(Log.DEBUG, "Unregistered for wifi updates");
		this.unregisterReceiver(wifiReceiver);
	}

	protected void onStop() {
		super.onStop();
		UiUpdater.unregisterClient(handler);
	}

	protected void onDestroy() {
		super.onDestroy();
		UiUpdater.unregisterClient(handler);
	}

	private void configSetting() {
		// Validation was successful, save the settings object
		SharedPreferences settings = getSharedPreferences(
				Defaults.getSettingsName(), Defaults.getSettingsMode());
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(CONFIG_KEYS.USERNAME, Defaults.username);
		editor.putString(CONFIG_KEYS.PASSWORD, Defaults.password);
		editor.putInt(CONFIG_KEYS.PORTNUM, 2121);
		editor.putString(CONFIG_KEYS.CHROOTDIR, Defaults.chrootDir);
		editor.putBoolean(CONFIG_KEYS.ACCEPT_WIFI, Defaults.acceptWifi);
		editor.putBoolean(CONFIG_KEYS.ACCEPT_NET, Defaults.acceptNet);
		editor.putBoolean(CONFIG_KEYS.STAY_AWAKE, Defaults.stayAwake);
		editor.putBoolean(CONFIG_KEYS.IS_ANONYMOUS, Defaults.isAnonymous);
		editor.commit();

	}

	/**
	 * This will be called by the static UiUpdater whenever the service has
	 * changed state in a way that requires us to update our UI.
	 * 
	 * We can't use any myLog.l() calls in this function, because that will
	 * trigger an endless loop of UI updates.
	 */

	public void updateUi() {
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		int wifiState = wifiMgr.getWifiState();
		myLog.l(Log.DEBUG, "Updating UI", true);
		if (FTPServerService.isRunning()) {
			myLog.l(Log.DEBUG, "updateUi: server is running", true);
			// Put correct text in start/stop button
			_startStop_Button.setText(R.string.stop_server);
			
			// Fill in wifi status and address
			InetAddress address = FTPServerService.getWifiIp();
			if (address != null) {
				_ip_TextView.setText("ftp://" + address.getHostAddress() + ":"
						+ FTPServerService.getPort() + "/");

				_ftpStatus_imageView.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.ftp_on));

			} else {
				myLog.l(Log.VERBOSE, "Null address from getServerAddress()",
						true);
				_ip_TextView.setText(R.string.cant_get_url);
				_ftpStatus_imageView.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.ftp_off));
			}
		} else {
			myLog.l(Log.DEBUG, "updateUi: server is not running", true);
			// Update the start/stop button to show the correct text
			_startStop_Button.setText(R.string.start_server);
			_ip_TextView.setText(R.string.no_url_yet);
			_startStop_Button.setText(R.string.start_server);
			_ftpStatus_imageView.setBackgroundDrawable(getResources()
					.getDrawable(R.drawable.ftp_off));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@SuppressLint("ShowToast")
	public void onCreateDialog() {
		Toast.makeText(getApplicationContext(), "파일 이름 : " + filename,
				Toast.LENGTH_SHORT).show();
	}

	public void Changebg(String ip) {
		Toast.makeText(getApplicationContext(), "클라이업트 접속!  IP : " + ip,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "홈 디렉토리 설정");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == Menu.FIRST) {
			Intent Directory_List = new Intent(this, Directory_List.class);
			startActivityForResult(Directory_List, CALLER_REQUEST);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
		if (requestCode == CALLER_REQUEST) {
			if (resultCode == RESULT_OK) {

				String path = intent.getExtras().get("path").toString();
				File file = new File(path);
				try {
					Globals.setChrootDir(file.getCanonicalFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_directory.setText(path);

			}
		}
	}

	OnClickListener startStopListener = new OnClickListener() {
		public void onClick(View v) {

			Context context = getApplicationContext();
			Intent intent = new Intent(context, FTPServerService.class);
			/*
			 * In order to choose whether to stop or start the server, we check
			 * the text on the button to see which action the user was
			 * expecting.
			 */

			String startString = getString(R.string.start_server);
			String stopString = getString(R.string.stop_server);
			String buttonText = _startStop_Button.getText().toString();
			if (buttonText.equals(startString)) {
				/* The button had the "start server" text */
				if (!FTPServerService.isRunning()) {
					myLog.e("context");
					warnIfNoExternalStorage();
					context.startService(intent);
					Toast.makeText(getApplicationContext(),
							"클라이언트에 이 IP주소를 입력해 주세요.", Toast.LENGTH_SHORT)
							.show();
				}
			} else if (buttonText.equals(stopString)) {
				/*
				 * The button had the "stop server" text. We stop the server
				 * now.
				 */
				context.stopService(intent);
			} else {
				// Do nothing
				myLog.l(Log.ERROR, "Unrecognized start/stop text");
			}
		}
	};

	private void warnIfNoExternalStorage() {
		String storageState = Environment.getExternalStorageState();
		if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
			myLog.i("Warning due to storage state " + storageState);
			Toast toast = Toast.makeText(this, R.string.storage_warning,
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	OnClickListener addUserListener = new OnClickListener() {
		public void onClick(View v) {
			myLog.l(Log.INFO, "Add user stub");
		}
	};

	OnClickListener manageUsersListener = new OnClickListener() {
		public void onClick(View v) {
			myLog.l(Log.INFO, "Manage users stub");
		}
	};

	OnClickListener serverOptionsListener = new OnClickListener() {
		public void onClick(View v) {
			myLog.l(Log.INFO, "Server options stub");
		}
	};

	DialogInterface.OnClickListener ignoreDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		}
	};

	/**
	 * A call-back for when the user presses the "setup" button.
	 */
	OnClickListener setupListener = new OnClickListener() {
		public void onClick(View v) {
			launchCONFIG_KEYS();
		}
	};

	void launchCONFIG_KEYS() {
		if (!requiredSettingsDefined()) {
			Toast toast = Toast.makeText(this, R.string.must_config,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		Intent intent = new Intent(activityContext, CONFIG_KEYS.class);
		startActivity(intent);
	}

	/**
	 * A callback for when the user toggles the session monitor on or off
	 */
	OnClickListener sessionMonitorCheckBoxListener = new OnClickListener() {
		public void onClick(View v) {
			// Trigger a UI update message to our Activity
			UiUpdater.updateClients();
			// updateUi();
		}
	};

	/**
	 * A callback for when the user toggles the server log on or off
	 */
	OnClickListener serverLogCheckBoxListener = new OnClickListener() {
		public void onClick(View v) {
			// Trigger a UI update message to our Activity
			UiUpdater.updateClients();
			// updateUi();
		}
	};

	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		public void onReceive(Context ctx, Intent intent) {
			myLog.l(Log.DEBUG, "Wifi status broadcast received");
			updateUi();
		}
	};

	boolean requiredSettingsDefined() {
		SharedPreferences settings = getSharedPreferences(
				Defaults.getSettingsName(), Defaults.getSettingsMode());
		String username = settings.getString("username", null);
		String password = settings.getString("password", null);
		if (username == null || password == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the settings from the FTPServerService if it's running, otherwise
	 * load the settings directly from persistent storage.
	 */
	SharedPreferences getSettings() {
		SharedPreferences settings = FTPServerService.getSettings();
		if (settings != null) {
			return settings;
		} else {
			return this.getPreferences(MODE_PRIVATE);
		}
	}
}
