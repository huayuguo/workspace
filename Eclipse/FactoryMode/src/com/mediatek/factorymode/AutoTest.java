package com.mediatek.factorymode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.mediatek.factorymode.wifi.WiFiTools;
import java.util.List;

public class AutoTest extends Activity
{
  Runnable bluerunnable;
  boolean isregisterReceiver = false;
  private BluetoothAdapter mAdapter = null;
  boolean mBlueFlag = false;
  BlueHandler mBlueHandler;
  boolean mBlueResult = false;
  boolean mBlueStatus = false;
  HandlerThread mBlueThread;
  boolean mOtherOk = false;
  BroadcastReceiver mReceiver;
  boolean mSdCardResult = false;
  SharedPreferences mSp;
  boolean mWifiConReslut = false;
  WifiHandler mWifiHandler;
  List mWifiList = null;
  boolean mWifiResult = false;
  boolean mWifiStatus = false;
  HandlerThread mWifiThread;
  WiFiTools mWifiTools;
  Message msg = null;
  ProgressDialog pDialog;
  Runnable wifirunnable;
  class AutoTest1 implements Runnable
  {
    public void run()
    {
      long l = 3000L;
      if (!mWifiStatus)
      {
        if (WifiInit())
        {
          mWifiResult = true;
          /*mWifiHandler.postDelayed(this, l);
        if (mWifiTools.IsConnection().booleanValue())
        {
          mWifiResult = true;
         mWifiTools.closeWifi();
        }
        mWifiHandler.postDelayed(this, l);*/
      }
        else
        	mWifiHandler.postDelayed(wifirunnable, l);
     }
    }
  }
    private BroadcastReceiver AutoTest2 = new BroadcastReceiver()
    {
  		@Override
      public void onReceive(Context paramContext, Intent paramIntent)
      {
        boolean i = true;
        String str = paramIntent.getAction();
        if ((!"android.bluetooth.device.action.FOUND".equals(str)) ||
      		  (((BluetoothDevice)paramIntent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")).getBondState() == 12))
          return;
        mBlueResult = i;
      }		
  };
  class AutoTest3 implements Runnable
  {
    public void run()
    {
      BlueInit();
    }
  }
  public AutoTest()
  {
    HandlerThread localHandlerThread1 = new HandlerThread("blueThread");
    this.mBlueThread = localHandlerThread1;
    HandlerThread localHandlerThread2 = new HandlerThread("wifiThread");
    this.mWifiThread = localHandlerThread2;
    this.pDialog = null;
    AutoTest1 local1 = new AutoTest1();
    this.wifirunnable = local1;
    this.mReceiver = AutoTest2;
    AutoTest3 local3 = new AutoTest3();
    this.bluerunnable = local3;
  }

  public void BackstageDestroy()
  {
    this.mWifiTools.closeWifi();
    BlueHandler localBlueHandler = this.mBlueHandler;
    Runnable localRunnable1 = this.bluerunnable;
    localBlueHandler.removeCallbacks(localRunnable1);
    WifiHandler localWifiHandler = this.mWifiHandler;
    Runnable localRunnable2 = this.wifirunnable;
    localWifiHandler.removeCallbacks(localRunnable2);
    if (this.isregisterReceiver == true)
    {
      BroadcastReceiver localBroadcastReceiver = this.mReceiver;
      unregisterReceiver(localBroadcastReceiver);
      this.isregisterReceiver = false ;
    }
    this.mAdapter.disable();
  }

  public void BlueInit()
  {
    BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    this.mAdapter = localBluetoothAdapter;
    this.mAdapter.enable();
    if (this.mAdapter.isEnabled() == true)
    {
      StartReciver();
     
        if (!this.mAdapter.startDiscovery())
        this.mAdapter.startDiscovery();
    }
    else{
    BlueHandler localBlueHandler = this.mBlueHandler;
    Runnable localRunnable = this.bluerunnable;
    localBlueHandler.postDelayed(localRunnable, 3000L);}
  }

  public void OnFinish()
  {
    boolean bool1 = true;
    String str1 = "failed";
    String str2 = "success";
    SharedPreferences localSharedPreferences = this.mSp;
    Utils.SetPreferences(this, localSharedPreferences, R.string.backlight_name, str2);//backlight_name
    Utils.SetPreferences(this, localSharedPreferences, R.string.memory_name, str2);//memory_name
    int i = 0;
    if(mSdCardResult)
    {
    	i = R.string.sdcard_name;
        Utils.SetPreferences(this, localSharedPreferences, i, str2);
    }
    else
    	 Utils.SetPreferences(this, localSharedPreferences,R.string.sdcard_name, str1);
    if(mWifiResult)
    {
    	i = R.string.wifi_name;
        Utils.SetPreferences(this, localSharedPreferences, i, str2);
    }
    else
    	Utils.SetPreferences(this,localSharedPreferences, R.string.wifi_name, str1);
    if(mBlueResult)
    {
    	i = R.string.bluetooth_name;
        Utils.SetPreferences(this, localSharedPreferences, i, str2);
    }
    else
    	Utils.SetPreferences(this, localSharedPreferences, R.string.bluetooth_name, str1);
    finish();
    
  }

  public void SdCardInit()
  {
    if (!Environment.getExternalStorageState().equals("mounted"))
      return;
    this.mSdCardResult = true;
  }

  public void StartReciver()
  {
    IntentFilter localIntentFilter = new IntentFilter("android.bluetooth.device.action.FOUND");
    BroadcastReceiver localBroadcastReceiver = this.mReceiver;
    registerReceiver(localBroadcastReceiver, localIntentFilter);
    this.isregisterReceiver = true;
  }

  public boolean WifiInit()
  {

	    boolean bool1 = false; 
	    boolean mResult = false;
	    String str = "";
	    String mNetWorkName ;
			List<ScanResult> scanResults = this.mWifiTools.scanWifi();
		    this.mWifiList = (List)scanResults;
	    mNetWorkName = str;
		    if(scanResults!=null){		    	
		    for (int i = 0; i < scanResults.size(); i++) {
	         ScanResult localScanResult = scanResults.get(i);
				if (!localScanResult.capabilities.equals(str))
		          {
					StringBuilder StringBuilder1 = new StringBuilder()
							.append(mNetWorkName).append(localScanResult.SSID)
							.append("\n");
					mNetWorkName = StringBuilder1.toString();}}
				for (int i = 0; i < scanResults.size(); i++) {
					ScanResult localScanResult = scanResults.get(i);
					if (!localScanResult.capabilities.equals("[WPS]")) {
		            if (! localScanResult.capabilities.equals(str))
		              continue;
		          }
					mResult = mWifiTools.addWifiConfig(mWifiList,
							localScanResult, str);
	          if(!mResult)
	         	 continue;
	        }
				if(mNetWorkName!=null&&mNetWorkName.length()!=0)
					bool1 = true;
		    }

		    	return bool1;
  }

  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    System.gc();
    Intent localIntent = new Intent();

    int i = -1;
    if (paramInt1 == 16)
    {
      if (paramInt2 == 1)
      {
        finish();
        return;
      }
      else
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.KeyCode");
        i = 512;
      }
    }

      if (paramInt1 == 512)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.vibrator.Vibrator");
        i = 128;
      }
      if (paramInt1 == 128)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.signal.Signal");
        i = 144;
      }
      if (paramInt1 == 144)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.earphone.Earphone");
        i = 368;
      }
      if (paramInt1 == 368)
      {
        localIntent.setClassName(this, "com.zte.engineer.GsensorCalibration");
        i = 304;
      }
      if (paramInt1 == 304)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.sensor.MSensor");
        i = 320;
      }
      if (paramInt1 == 320)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.sensor.PSensor");
        i = 352;
      }
      if (paramInt1 == 352)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.sensor.LSensor");
        i = 336;
      }
      if (paramInt1 == 336)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.simcard.SimCard");
        i = 515;
      }
      if (paramInt1 == 515)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.microphone.MicRecorder");
        i = 514;
      }
      if (paramInt1 == 514)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.headset.HeadSet");
        i = 288;
      }
      if (paramInt1 == 288)
      {
//        localIntent.setClassName(this, "com.mediatek.factorymode.fmradio.FMRadio");
    	  localIntent.setClassName(this, "com.mediatek.factorymode.fmradio.FmTest2");
    	  
    	  i = 513;
      }
     if (paramInt1 == 513)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.camera.SubCamera");
        i = 516;
      }
      if (paramInt1 == 516)
      {
        localIntent.setClassName(this, "com.mediatek.factorymode.camera.CameraTest");
        i = 64;  
      }
      
      if (paramInt1 == 64)
        OnFinish();
      else
      startActivityForResult(localIntent, i);
    }
 
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.alltest);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
     WiFiTools localWiFiTools = new WiFiTools(this);
    this.mWifiTools = localWiFiTools;
    this.mWifiTools.openWifi();
    this.mWifiThread.start();
    Looper localLooper1 = this.mWifiThread.getLooper();
    WifiHandler localWifiHandler1 = new WifiHandler(localLooper1);
    this.mWifiHandler = localWifiHandler1;
    WifiHandler localWifiHandler2 = this.mWifiHandler;
    Runnable localRunnable1 = this.wifirunnable;
    localWifiHandler2.post(localRunnable1);
    this.mBlueThread.start();
    Looper localLooper2 = this.mBlueThread.getLooper();
    BlueHandler localBlueHandler1 = new BlueHandler(localLooper2);
    this.mBlueHandler = localBlueHandler1;
    BlueHandler localBlueHandler2 = this.mBlueHandler;
    Runnable localRunnable2 = this.bluerunnable;
    localBlueHandler2.post(localRunnable2);
    SdCardInit();
    Intent localIntent = new Intent();
    localIntent.setClassName(this, "com.mediatek.factorymode.BatteryLog");
    startActivityForResult(localIntent, 16);
  }

  public void onDestroy()
  {
    super.onDestroy();
    BackstageDestroy();
  }

  class BlueHandler extends Handler
  {
    public BlueHandler()
    {
    }

    public BlueHandler(Looper arg2)
    {
     super(arg2);
    }

    public void handleMessage(Message paramMessage)
    {
      super.handleMessage(paramMessage);
    }
  }

  class WifiHandler extends Handler
  {
    public WifiHandler()
    {
    }

    public WifiHandler(Looper arg2)
    {
      super(arg2);
    }

    public void handleMessage(Message paramMessage)
    {
      super.handleMessage(paramMessage);
    }
  }
}
