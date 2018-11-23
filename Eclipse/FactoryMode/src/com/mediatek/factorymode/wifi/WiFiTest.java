package com.mediatek.factorymode.wifi;

/*import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiTest extends Activity implements View.OnClickListener
{
    private static final String TAG = "TestWiFi";
    private Button mBtFailed = null;
    private Button mBtOk = null;
    private WifiManager mWifiManager = null; 
    private TextView wifi_state;
    private TextView wifi_result;
    private TextView wifi_con;
    private int mWifiState = -1;
    SharedPreferences mSp;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_test);
        mSp = getSharedPreferences("FactoryMode", 0);
        initView();
    }

    private void initView() 
    {
        wifi_state = (TextView)findViewById(R.id.wifi_state_id);
        wifi_result = (TextView)findViewById(R.id.wifi_result_id);
        wifi_con = (TextView)findViewById(R.id.wifi_con_id);
        mBtOk = (Button)findViewById(R.id.wifi_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button)findViewById(R.id.wifi_bt_failed);
        mBtFailed.setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        changeWifiState(0);
    }

    private void changeWifiState(int state) 
    {
        switch (state) 
        {
        case 0: 
        {
            new Thread() 
            {
                public void run() 
                {
                    Log.e(TAG, "setWifiEnabled(true)");
                    mWifiManager.setWifiEnabled(true);
                }
            }.start();

            while ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) || 
                   (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)) 
            {
                if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
                    wifi_state.setText(R.string.WiFi_info_open);
                }
                break;
            }
            mWifiState = 1;
            //��ȡ�����б?��������ѡ����
            wifi_result.setText(R.string.WiFi_connecting);
            List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
            List wifilist = mWifiManager.getScanResults();
            if(configurations.size() > 0){
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < configurations.size(); i++)
                {
                    int netID = configurations.get(0).networkId;
                    sb.append(configurations.get(i).SSID).append("\n");
                    if(i == 0)
                    {
                        mWifiManager.enableNetwork(netID, true);
                    }
                }
                wifi_con.setText(sb.toString());
            }
            else
                wifi_con.setText(R.string.WiFi_notfound_openap);
            
            break;
        }
        case 1:
        {
            new Thread() 
            {
                public void run() 
                {
                    Log.e(TAG, "setWifiEnabled(false)");
                    mWifiManager.setWifiEnabled(false);
                }
            }.start();

            while ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) ||
                   (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED))
            {
                wifi_state.setText(R.string.WiFi_info_closeing);
                break;
            }
            wifi_result.setText(R.string.WiFi_info_unknown);
            wifi_con.setText(R.string.WiFi_info_unknown);
            mWifiState = 0;
            break;
        }
        default:
            Log.e(TAG, "Wifi state error !");
            break;
        }
    }

    public void onClick(View v) {
        SharedPreferences localSharedPreferences = this.mSp;
        changeWifiState(1);
        if(v.getId() == this.mBtOk.getId())
        {
            Utils.SetPreferences(this, localSharedPreferences,R.string.wifi_name, "success");
            finish();
        }
        else{
            Utils.SetPreferences(this, localSharedPreferences,R.string.wifi_name, "failed");
            finish();
        }
    }
}*/

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.content.IntentFilter;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
//import dalvik.annotation.Signature;
import java.util.List;

public class WiFiTest extends Activity
  implements View.OnClickListener
{
  public static final int TIMEOUT = 15;
  private static final int WIFI_CONNECTED = 2;
  private static final int WIFI_CONNECTING = 3;
  private static final int WIFI_FAILED = 4;
  private static final int WIFI_LIST = 1;
  private static final int WIFI_NOTFOUND_OPENAP = 5;
  private static final int WIFI_STATE = 0;
  public static final int WIFI_STATE_DISABLED = 1;
  public static final int WIFI_STATE_DISABLING = 0;
  public static final int WIFI_STATE_ENABLED = 3;
  public static final int WIFI_STATE_ENABLING = 2;
  public static final int WIFI_STATE_STRENGTH = 6;
  //Handler UiHandler;
  private Button mBtFailed = null;
  private Button mBtOk = null;
  private int mCount;
  boolean mFlag = false;
  WifiHandler mHandler;
  boolean mListFlag;
  private String mNetWorkName = "";
  private String mStrengthName = "";
  boolean mResult = false;
  private SharedPreferences mSp = null;
  private TextView mTvCon = null;
  private TextView mTvInfo = null;
  private TextView mTvResInfo = null;
  private TextView mTvResult = null;
  private TextView mSstrength = null;

  //@Signature({"Ljava/util/List", "<", "Landroid/net/wifi/ScanResult;", ">;"})
  private List mWifiList = null;
  boolean mWifiScan;
  HandlerThread mWifiThread;
  private WiFiTools mWifiTools;
  private WifiStateReceiver wifiReceiver;

	  private Runnable wifirunnable = new Runnable(){
      public void run() {
        // TODO Auto-generated method stub
        long l = 3000L;
        mCount++;
        if (mCount >= 15)
        {
          UiHandler.sendEmptyMessage(4);
          mWifiTools.closeWifi();
          mHandler.removeCallbacks(this);
        }
       if (!mFlag)
        {
          boolean bool = StartWifi();
         
          if ((!bool) && (mListFlag))
          {
            UiHandler.sendEmptyMessage(5);
            mHandler.removeCallbacks(this);
            return;
          }
          if (bool)
          {
            mFlag = true;
            UiHandler.sendEmptyMessage(3);
          }
          mHandler.postDelayed(this, l);
        }
        while (mWifiTools.mResult)
        {
          if (mWifiTools.IsConnection())
          {
              UiHandler.sendEmptyMessage(2);//WIFI_STATE_ENABLING
              mHandler.postDelayed(this, l);
               break ;
          }
        }
      }
  };
  private Handler UiHandler = new Handler(){
      public void handleMessage(Message paramMessage)
      {
        switch (paramMessage.what)
        {
        case 0://state
        	String state = mWifiTools.GetState();
        	if(state!=null)
            mTvInfo.setText(state);
            mTvResult.setText(getString(R.string.WiFi_scaning));//WiFi_scaning
             break;
        case 1://list
        	mTvResult.setText(mNetWorkName);
            break;
        case 2://connected
        	mTvResInfo.setText(mWifiTools.GetWifiInfo().toString());
        	mTvInfo.setText(getString(R.string.WiFi_success));
               UiHandler.sendEmptyMessage(6);//WIFI_STATE_STRENGTH    //lhy add 
        	break;
        case 3://connectting
        	mTvResInfo.setText(getString(R.string.WiFi_connecting));
        	mTvResInfo.setTextColor(getResources().getColor(R.color.Green));
        	break;
        case 4://fail
        	mTvResInfo.setText(getString(R.string.WiFi_failed));
        	mTvResInfo.setTextColor(getResources().getColor(R.color.Red));
        	break;
        case 5://WIFI_NOTFOUND_OPENAP
            mTvResInfo.setText(getString(R.string.WiFi_notfound_openap));
            mTvResInfo.setTextColor(getResources().getColor(R.color.Red));           
        	break;
   //lhy 
        case 6://WIFI_STATE_STRENGTH
       // 	mSstrength.setText(mWifiTools.Getsinglestrength());       	
        //	mSstrength.setTextColor(getResources().getColor(R.color.Red));           
        	break;       	
        	
        	
        default:
        	break;
        }
      }
  };
  public WiFiTest()
  {
    this.mWifiThread = new HandlerThread("wifiThread");
   // this.wifirunnable = new WifiTestRun();
    //this.UiHandler = new WifiTestHandler();
  }


public boolean StartWifi()
  {
		   // Object localObject1 = 0;
		    String str1 = "";
		    UiHandler.sendEmptyMessage(0);
			List<ScanResult> scanResults = this.mWifiTools.scanWifi();
		    this.mWifiList = (List)scanResults;
//		    this.mNetWorkName = str1;
		    if(scanResults!=null){		    	
		    for (int i = 0; i < scanResults.size(); i++) {
	            ScanResult localScanResult = scanResults.get(i);
				if (!localScanResult.capabilities.equals(str1))
		          {
					StringBuilder StringBuilder1 = new StringBuilder()
							.append(mNetWorkName).append(localScanResult.SSID)
							.append("\n");
					this.mNetWorkName = StringBuilder1.toString();}}
				for (int i = 0; i < scanResults.size(); i++) {
					ScanResult localScanResult = scanResults.get(i);
					if (!localScanResult.capabilities.equals("[WPS]")) {
		            if (! localScanResult.capabilities.equals(str1))
		              continue;
		          }
					mResult = mWifiTools.addWifiConfig(mWifiList,
							localScanResult, str1);
                 if(!mResult)
                	 continue;
	           }
				if(mNetWorkName!=null&&mNetWorkName.length()!=0){
				mListFlag = true;
		    this.UiHandler.sendEmptyMessage(1);}
		    }
		   // else
		    	return mResult;

  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.wifi_name, "success");
        finish();
    }else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.wifi_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.wifi_test);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mTvInfo = (TextView)findViewById(R.id.wifi_state_id);
    this.mTvResult = (TextView)findViewById(R.id.wifi_result_id);
    this.mTvCon = (TextView)findViewById(R.id.wifi_con_id);
    this.mTvResInfo = (TextView)findViewById(R.id.wifi_resinfo_id);
    this.mSstrength = (TextView)findViewById(R.id.wifi_strength);
    
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed.setOnClickListener(this);
    this.mWifiTools = new WiFiTools(this);
    mWifiScan = this.mWifiTools.openWifi();
    this.mWifiThread.start();
    Looper localLooper = this.mWifiThread.getLooper();
    this.mHandler = new WifiHandler(localLooper);
    this.mHandler.post(this.wifirunnable);
    
	//WIFI״̬������
	//WifiStateReceiver wifiReceiver=new WifiStateReceiver(this,mSstrength);
	 wifiReceiver=new WifiStateReceiver(this,mSstrength);
	IntentFilter filter=new IntentFilter();
	filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
	filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	this.registerReceiver(wifiReceiver,filter); 

    
    
    
    
    
    
    
    
  }

  public void onDestroy()
  {
this.unregisterReceiver(wifiReceiver);

    super.onDestroy();
    if(!mWifiScan)
    this.mWifiTools.closeWifi();
    this.mHandler.removeCallbacks(wifirunnable);
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
