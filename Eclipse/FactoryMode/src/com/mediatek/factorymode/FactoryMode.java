package com.mediatek.factorymode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings; //Added by qxbin@20130819
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.util.Log;
//import com.mediatek.common.featureoption.FeatureOption;

import android.hardware.SensorManager;    // tongjun@yjzn 
import android.hardware.Sensor;
//import dalvik.annotation.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactoryMode extends Activity implements
		AdapterView.OnItemClickListener {
	public View.OnClickListener cl;
	private int[] itemString;
	private MyAdapter mAdapter;
	private Button mBtAll;
	private Button mBtAuto;
	private GridView mGrid;
	private String TAG = "FactoryMode";
	// @Signature({"Ljava/util/List", "<", "Ljava/lang/String;", ">;"})
	private List mListData;
	private SharedPreferences mSp = null;

	public FactoryMode() {
	
	}
	/**
	 * this method should be called in after onCreate
	 */
	private void filterUnsupportedCases(){
		List<Integer> testItems = new ArrayList<Integer>(Arrays.asList(
				R.string.touchscreen_name,
				R.string.lcd_name, 
				R.string.gps_name, 
				R.string.battery_name,
				R.string.KeyCode_name, 
				R.string.speaker_name,
				R.string.headset_name, 
				R.string.microphone_name,
				R.string.earphone_name,
				R.string.wifi_name,
				R.string.bluetooth_name, 
				R.string.vibrator_name,
				//R.string.telephone_name,
				R.string.backlight_name,
				R.string.memory_name, 
				//R.string.gsensorcali_name,
				R.string.gsensor_name,
				R.string.msensor_name, 
				R.string.lsensor_name, 
				R.string.psensor_name,
				R.string.gyroscope,
				R.string.sdcard_name,
				R.string.camera_name,
				R.string.subcamera_name, 
				R.string.fmradio_name,
				R.string.sim_name, 
				R.string.flashlight,
				R.string.temphumiditytester,
				R.string.nfc_name,
				/*R.string.tag_Identification_name,
				R.string.scanner_name,
				R.string.power_meter_reading_name,
				R.string.infrared_thermometer,*/
				R.string.ti_ble,
				R.string.ma_fingerprint,
				R.string.device_info,
				//R.string.serial_test,
				//R.string.GPIO,
				R.string.factory_reset,
				R.string.version ));
		// filter unsupported sensor test case 
		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(mSensorManager !=null){
			Map<Integer, Integer> typeIDMap = new HashMap<Integer, Integer>();
			typeIDMap.put(Sensor.TYPE_ACCELEROMETER, R.string.gsensor_name);
		//	typeIDMap.put(Sensor.TYPE_GYROSCOPE, 6);
			typeIDMap.put(Sensor.TYPE_MAGNETIC_FIELD, R.string.msensor_name);
			typeIDMap.put(Sensor.TYPE_LIGHT, R.string.lsensor_name);
			typeIDMap.put(Sensor.TYPE_PROXIMITY, R.string.psensor_name);
			for(Integer key: typeIDMap.keySet()){
				if(mSensorManager.getDefaultSensor(key)==null){
					testItems.remove(typeIDMap.get(key));
				}
			}
		}

		// check to see flashlight, use camera.flashlight.exist
		if(android.os.SystemProperties.getBoolean("camera.flashlight.exist", true)==false){
			// remove flashlight test
			testItems.remove(new Integer(R.string.flashlight));
		}
		// check to see DISABLE_EARPICE 
		if(android.os.SystemProperties.getBoolean("ro.mtk_disable_earpiece", false)==true){
			// remove flashlight test
			testItems.remove(new Integer(R.string.earphone_name));
		}		
		// rebuild array
		this.itemString = new int[testItems.size()];
		for(int i=0; i<testItems.size(); i++){
			this.itemString[i] = testItems.get(i);
		}
		android.util.Log.i("tong", Arrays.toString(this.itemString));
	}
	private void SetColor(TextView paramTextView) {
		if (this.itemString.length == 0)
			return;
		SharedPreferences localSharedPreferences1 = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences1;
		int localObject = 0;
		int i = this.itemString.length;
		while (true) {
			if (localObject >= i)
				break;
			Resources localResources = getResources();
			int j = this.itemString[localObject];
			String str1 = localResources.getString(j);
			String str2 = paramTextView.getText().toString();
			String str4;
			if (str1.equals(str2)) {
				SharedPreferences localSharedPreferences2 = this.mSp;
				int k = this.itemString[localObject];
				String str3 = getString(k);
				str4 = localSharedPreferences2.getString(str3, null);

				if (str4.equals("success")) {
					int l = getApplicationContext().getResources().getColor(
							R.color.Blue);
					// paramTextView.setTextColor(l);
					paramTextView
							.setBackgroundResource(R.drawable.yjzn_btn_success);
				} else if (str4.equals("default")) {
					int i1 = getApplicationContext().getResources().getColor(
							R.color.black);
					// paramTextView.setTextColor(i1);
					paramTextView
							.setBackgroundResource(R.drawable.yjzn_btn_default);

				} else if (str4.equals("failed")) {
					int i2 = getApplicationContext().getResources().getColor(
							R.color.Red);
					// paramTextView.setTextColor(i2);
					paramTextView
							.setBackgroundResource(R.drawable.yjzn_btn_fail);

				}

			}
			++localObject;
		}
	}

	// @Signature({"()", "Ljava/util/List", "<", "Ljava/lang/String;", ">;"})
	private List getData() {
		boolean bool1 = true;
		ArrayList localArrayList = new ArrayList();
		SharedPreferences localSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		int localObject = 0;
		while (true) {
			int i = this.itemString.length;
			if (localObject >= i)
				break;
			int j = this.itemString[localObject];
			String str1 = getString(j);
			boolean bool2 = localSharedPreferences.getBoolean(str1, bool1);
			if (bool1 == bool2) {
				int k = this.itemString[localObject];
				String str2 = getString(k);
				localArrayList.add(str2);
			}
			++localObject;
		}
		return localArrayList;
	}

	private void init() {
		String str1 = "default";
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences;
		SharedPreferences.Editor localEditor = this.mSp.edit();
		int localObject = 0;
		while (true) {
			int i = this.itemString.length;
			if (localObject >= i)
				break;
			int j = this.itemString[localObject];
			String str2 = getString(j);
			String exist = localSharedPreferences.getString(str2, null);
			if (exist == null)
				localEditor.putString(str2, str1);
			++localObject;
		}
		String str3 = getString(R.string.headsethook_name);
		localEditor.putString(str3, str1);
		localEditor.commit();
	}

	protected void onActivityResult(int paramInt1, int paramInt2,
			Intent paramIntent) {
		System.gc();
		Intent localIntent = new Intent();
		localIntent.setClassName(this, "com.mediatek.factorymode.Report");
		startActivity(localIntent);
	}

	public void onCreate(Bundle paramBundle) {
		requestWindowFeature(1);
		super.onCreate(paramBundle);
		setContentView(R.layout.main);
		filterUnsupportedCases();
		init();
		Log.e(TAG, "onCreate");

		/*
		 * this.mBtAuto = (Button)findViewById(R.id.main_bt_autotest);
		 * this.mBtAuto.setOnClickListener(new View.OnClickListener(){ public
		 * void onClick(View v) { String str = "com.mediatek.factorymode";
		 * Intent intent = new Intent(); intent.setClassName(str,
		 * "com.mediatek.factorymode.AutoTest");
		 * FactoryMode.this.startActivityForResult(intent, 4096); } });
		 * 
		 * this.mBtAll = (Button)findViewById(R.id.main_bt_alltest);
		 * this.mBtAll.setOnClickListener(new View.OnClickListener(){ public
		 * void onClick(View v) { String str = "com.mediatek.factorymode";
		 * Intent intent = new Intent(); intent.setClassName(str,
		 * "com.mediatek.factorymode.AllTest");
		 * FactoryMode.this.startActivityForResult(intent, 8192); } });
		 */
		this.mGrid = (GridView) findViewById(R.id.main_grid);
		this.mListData = getData();
		this.mAdapter = new MyAdapter(this);
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy");

		Process.killProcess(Process.myPid());
	}

	// @Signature({"(", "Landroid/widget/AdapterView", "<*>;",
	// "Landroid/view/View;", "IJ)V"})
	public void onItemClick(AdapterView paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		Intent localIntent = new Intent();
		localIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		String str1 = (String) this.mListData.get(paramInt);
		String str2 = null;
		Log.e(TAG, "onItemClick");

		if (getString(R.string.speaker_name).equals(str1))
			str2 = "com.mediatek.factorymode.audio.AudioTest";
		else if (getString(R.string.battery_name).equals(str1))
			str2 = "com.mediatek.factorymode.BatteryLog";
		else if (getString(R.string.touchscreen_name).equals(str1))
			str2 = "com.mediatek.factorymode.touchscreen.TouchPadTest";
		else if (getString(R.string.camera_name).equals(str1))
			str2 = "com.mediatek.factorymode.camera.CameraTest";
		else if (getString(R.string.wifi_name).equals(str1))
			str2 = "com.mediatek.factorymode.wifi.WiFiTest";
		else if (getString(R.string.bluetooth_name).equals(str1))
			str2 = "com.mediatek.factorymode.bluetooth.Bluetooth";
		else if (getString(R.string.headset_name).equals(str1))
			str2 = "com.mediatek.factorymode.headset.HeadSet";
		else if (getString(R.string.earphone_name).equals(str1))
			str2 = "com.mediatek.factorymode.earphone.Earphone";
		else if (getString(R.string.vibrator_name).equals(str1))
			str2 = "com.mediatek.factorymode.vibrator.Vibrator";
		else if (getString(R.string.telephone_name).equals(str1))
			str2 = "com.mediatek.factorymode.signal.Signal";
		else if (getString(R.string.gps_name).equals(str1))
			str2 = "com.mediatek.factorymode.gps.GPS";
		else if (getString(R.string.backlight_name).equals(str1))
			str2 = "com.mediatek.factorymode.backlight.BackLight";
		else if (getString(R.string.memory_name).equals(str1))
			str2 = "com.mediatek.factorymode.memory.Memory";
		else if (getString(R.string.microphone_name).equals(str1))
			str2 = "com.mediatek.factorymode.microphone.MicRecorder";
		else if (getString(R.string.gsensor_name).equals(str1))
			str2 = "com.zte.engineer.GSensorTest";
		else if (getString(R.string.gsensorcali_name).equals(str1))
			str2 = "com.zte.engineer.GsensorCalibration";
		else if (getString(R.string.msensor_name).equals(str1))
			str2 = "com.mediatek.factorymode.sensor.MSensor";
		else if (getString(R.string.lsensor_name).equals(str1))
			str2 = "com.mediatek.factorymode.sensor.LSensor";
		else if (getString(R.string.gyroscope).equals(str1))
			str2 = "com.mediatek.factorymode.sensor.GySensor";
		else if (getString(R.string.psensor_name).equals(str1))
			str2 = "com.mediatek.factorymode.sensor.PSensor";
		else if (getString(R.string.sdcard_name).equals(str1))
			str2 = "com.mediatek.factorymode.sdcard.SDCard";
		else if (getString(R.string.fmradio_name).equals(str1)) { // Modified by
																	// qxbin@20130819
			str2 = "com.mediatek.factorymode.fmradio.FMTest";
			// Intent intent = new Intent().setClassName("com.mediatek.FMRadio",
			// "com.mediatek.FMRadio.FMRadioActivity");
			// startActivity(intent);
			// return;
			str2 = "com.mediatek.factorymode.fmradio.FmTest2";// by Jacky

		}else if(getString(R.string.device_info).equals(str1)){
			str2 = "com.mediatek.factorymode.deviceinfo.DeviceInfo";
		}else if(getString(R.string.ti_ble).equals(str1)){
			str2 = "com.mediatek.factorymode.TiBle";
		}else if(getString(R.string.factory_reset).equals(str1)){
			str2 = "com.mediatek.factorymode.MasterClear";
		}
		// ending
		else if (getString(R.string.KeyCode_name).equals(str1))
			str2 = "com.mediatek.factorymode.KeyCode";
		else if (getString(R.string.lcd_name).equals(str1))
			str2 = "com.mediatek.factorymode.lcd.LCD";
		else if (getString(R.string.sim_name).equals(str1))
			//str2 = "com.mediatek.factorymode.simcard.SimCard";
			str2 = "com.mediatek.factorymode.simcard.SimTest"; //zqf
			
		else if (getString(R.string.subcamera_name).equals(str1))
			str2 = "com.mediatek.factorymode.camera.SubCamera";
		else if (getString(R.string.led_name).equals(str1))
			str2 = "com.mediatek.factorymode.led.Led";

		else if (getString(R.string.version).equals(str1))
			str2 = "com.mediatek.factorymode.version.version";
		/* start add by Jacky */
		else if (getString(R.string.flashlight).equals(str1)) {
			str2 = "com.mediatek.factorymode.flashlight.FlashLight";

		} else if (getString(R.string.nfc_name).equals(str1)) {
			str2 = "com.mediatek.factorymode.nfc.Nfc";

		} else if (getString(R.string.tag_Identification_name).equals(str1)) {
			str2 = "com.mediatek.factorymode.tagidentification.TagIdentification";

		} else if (getString(R.string.scanner_name).equals(str1)) {
			str2 = "com.mediatek.factorymode.scanner.Scanner";

		} else if (getString(R.string.power_meter_reading_name).equals(str1)) {
			str2 = "com.mediatek.factorymode.powerMeterReading.PowerMeterReading";

		}  else if (getString(R.string.infrared_thermometer).equals(str1)) {
			str2 = "com.mediatek.factorymode.infraredThermometer.InfraredThermometer";

		} else if (getString(R.string.serial_test).equals(str1)) {
			str2 = "com.mediatek.factorymode.serial.SerialActivity";
		} else if (getString(R.string.temphumiditytester).equals(str1)){
			str2 = "com.mediatek.factorymode.TempHumidityTester.TempHumidityTester";
		} else if (getString(R.string.GPIO).equals(str1)) {
			Intent mIntent = new Intent();
			ComponentName cm = new ComponentName("com.mediatek.engineermode.io", "com.mediatek.engineermode.io.GpioActivity");
			mIntent.setComponent(cm);
			mIntent.setAction("android.intent.action.MAIN");  
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(mIntent);
		} else if(getString(R.string.ma_fingerprint).equals(str1)) {
			str2 = "com.mediatek.factorymode.fingerprint.MAFactoryActivity";
		}
		/* end */
		if (str2 != null) {
			Log.e(TAG, "str2");

			localIntent.setClassName(this, str2);
			startActivity(localIntent);
		}
	}

	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");

		this.mGrid.setAdapter(this.mAdapter);
		this.mGrid.setOnItemClickListener(this);
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context arg2) {
			// Context localContext;
			LayoutInflater localLayoutInflater = LayoutInflater.from(arg2);
			this.mInflater = localLayoutInflater;
		}

		public MyAdapter(FactoryMode paramInt, int arg3) {
		}

		public int getCount() {
			if (FactoryMode.this.mListData == null)
				return 0;
			return FactoryMode.this.mListData.size();
		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			View localView = this.mInflater.inflate(R.layout.main_grid, paramViewGroup, false);
			TextView localTextView = (TextView) localView
					.findViewById(R.id.factor_button);
			CharSequence localCharSequence = (CharSequence) FactoryMode.this.mListData
					.get(paramInt);
			localTextView.setText(localCharSequence);
			// Modified by qxbin@20130819
			/*
			 * if(localCharSequence.equals(getApplicationContext().getResources()
			 * .getString(R.string.fmradio_name))){ int result =
			 * Settings.System.getInt(getContentResolver(),
			 * Settings.System.FMRADIO_TEST_RESULT, 0);
			 * 
			 * if(result == 1){
			 * localTextView.setTextColor(getApplicationContext(
			 * ).getResources().getColor(R.color.Blue)); }else if(result == 2){
			 * localTextView
			 * .setTextColor(getApplicationContext().getResources().
			 * getColor(R.color.Red)); }else{
			 * localTextView.setTextColor(getApplicationContext
			 * ().getResources().getColor(R.color.black)); } }else{
			 * FactoryMode.this.SetColor(localTextView); }
			 */
			FactoryMode.this.SetColor(localTextView);
			// ending
/*
			int pWidth = mGrid.getWidth();
			int pHight = mGrid.getHeight();
			GridView.LayoutParams params = (LayoutParams) localTextView.getLayoutParams();
					params.width = pWidth / 3;
					params.height = pHight / 8;

			localView.setLayoutParams(params);
*/
			return localView;
		}
	}
}
