package com.mediatek.factorymode.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import android.content.IntentFilter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;


public class Bluetooth extends Activity
  implements View.OnClickListener
{
  Runnable bluerunnable;
  private BluetoothAdapter mAdapter = null;
  private boolean mBlueFlag = false;
  BlueHandler mBlueHandler;
  HandlerThread mBlueThread;
  private Button mBtFailed;
  private Button mBtOk;
  private Handler mHandler;
  private String mNameList = "";
  BroadcastReceiver mReceiver;
  private SharedPreferences mSp;
  private TextView mTvCon = null;
  private TextView mTvInfo = null;
  private TextView mTvResult = null;
  Message msg;
  Runnable myRunnable;
//׷��
  class Bluetooth4
  implements Runnable
{
  public void run()
  {
	  init();
  }
}
  
  class Bluetooth3 extends BroadcastReceiver
  {
    public void onReceive(Context paramContext, Intent paramIntent)
    {
      String str1 = paramIntent.getAction();
      if ("android.bluetooth.device.action.FOUND".equals(str1))
      {
        BluetoothDevice localBluetoothDevice = (BluetoothDevice)paramIntent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (localBluetoothDevice.getBondState() != 12)
        {
          //Bluetooth localBluetooth = this.this$0;
          StringBuilder localStringBuilder = new StringBuilder().append(mNameList);
          localStringBuilder.append(localBluetoothDevice.getName()).append("--");
          localStringBuilder.append(getString(R.string.Bluetooth_mac));
          localStringBuilder.append(localBluetoothDevice.getAddress()).append("\n");
          mNameList =localStringBuilder.toString();
          mTvResult.setText(mNameList);
         
        }
      }
    
        if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(str1))
        	mTvCon.setText(R.string.Bluetooth_scan_success);

    }
  }
  
  class Bluetooth2
  implements Runnable
{
  public void run()
  {
    if (mBlueFlag)//(mAdapter.enable())
    {

    	Handler localHandler1 = mHandler;
        Runnable localRunnable1 = myRunnable;
        localHandler1.removeCallbacks(localRunnable1);
       // Bluetooth.access$500(this.this$0);
        init();
      
    }

      Handler localHandler2 = mHandler;
      Runnable localRunnable2 = myRunnable;
      localHandler2.post(localRunnable2);

  }
}
  
  class Bluetooth1 extends Handler
  {
	  public void handleMessage(Message paramMessage)
	  {
	    super.handleMessage(paramMessage);
	    mTvInfo.setText(R.string.Bluetooth_open);
	    mTvResult.setText(R.string.Bluetooth_scaning);
	    Bluetooth.BlueHandler localBlueHandler = mBlueHandler;
	    Runnable localRunnable = bluerunnable;
	    localBlueHandler.removeCallbacks(localRunnable);
	    IntentFilter localIntentFilter1 = new IntentFilter("android.bluetooth.device.action.FOUND");
	    //Bluetooth localBluetooth1 = this.this$0;
	    BroadcastReceiver localBroadcastReceiver1 = mReceiver;
	    //localBluetooth1.
	    registerReceiver(localBroadcastReceiver1, localIntentFilter1);
	    IntentFilter localIntentFilter2 = new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
	    //Bluetooth localBluetooth2 = this.this$0;
	    BroadcastReceiver localBroadcastReceiver2 = mReceiver;
	    //localBluetooth2.
	    registerReceiver(localBroadcastReceiver2, localIntentFilter2);
	    if (!mAdapter.startDiscovery())
	    	mAdapter.startDiscovery();
	  }
  }
  
  public Bluetooth()
  {
    HandlerThread localHandlerThread = new HandlerThread("blueThread");
    this.mBlueThread = localHandlerThread;
    this.msg = null;
    Bluetooth1 local1 = new Bluetooth1();
    this.mHandler = local1;
    Bluetooth2 local2 = new Bluetooth2();
     this.myRunnable = local2;
    Bluetooth3 local3 = new Bluetooth3();
    this.mReceiver = local3;
    Bluetooth4 local4 = new Bluetooth4();
     this.bluerunnable = local4;
  }

  private void init()
  {

	this.mAdapter.enable();     
	 if (this.mAdapter.isEnabled() == true)//if (mBlueFlag == false)
    {
      Message localMessage = this.mHandler.obtainMessage();
      this.msg = localMessage;
      this.msg.sendToTarget();
      mBlueFlag = true;
    }  
	else
	{
      BlueHandler localBlueHandler = this.mBlueHandler;
      Runnable localRunnable = this.bluerunnable;
      localBlueHandler.postDelayed(localRunnable, 3000L);
	}
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    int i = R.string.bluetooth_name;
    int j = paramView.getId();
    int k = this.mBtOk.getId();
    int kk = this.mBtFailed.getId();
    if (j == k)
    {
      Utils.SetPreferences(this, localSharedPreferences, i, "success");
       finish();
    }
    if (j == kk)
    {
      Utils.SetPreferences(this, localSharedPreferences, i, "failed");
      finish();
    }   
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.ble_test);
    TextView localTextView1 = (TextView)findViewById(R.id.ble_state_id);
    this.mTvInfo = localTextView1;
    TextView localTextView2 = (TextView)findViewById(R.id.ble_result_id);
    this.mTvResult = localTextView2;
    TextView localTextView3 = (TextView)findViewById(R.id.ble_con_id);
    this.mTvCon = localTextView3;
    Button localButton1 = (Button)findViewById(R.id.bt_ok);
    this.mBtOk = localButton1;
    this.mBtOk.setOnClickListener(this);
    Button localButton2 = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed = localButton2;
    this.mBtFailed.setOnClickListener(this);
    BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    this.mAdapter = localBluetoothAdapter;
    this.mBlueThread.start();
    Looper localLooper = this.mBlueThread.getLooper();
    BlueHandler localBlueHandler1 = new BlueHandler(localLooper);
    this.mBlueHandler = localBlueHandler1;
    BlueHandler localBlueHandler2 = this.mBlueHandler;
    Runnable localRunnable = this.bluerunnable;
    localBlueHandler2.post(localRunnable);
    this.mTvInfo.setText(R.string.Bluetooth_opening);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    //init();
  }

  protected void onDestroy()
  {
    super.onDestroy();
    if (this.mBlueFlag == true)
    {
      BroadcastReceiver localBroadcastReceiver = this.mReceiver;
      unregisterReceiver(localBroadcastReceiver);
    }
    this.mAdapter.disable();
    mBlueFlag = false;
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
}