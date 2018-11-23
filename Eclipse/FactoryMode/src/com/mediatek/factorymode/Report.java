package com.mediatek.factorymode;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
//import dalvik.annotation.Signature;
import java.util.ArrayList;
import java.util.List;

public class Report extends Activity
{
  final int[] itemString;
  private TextView mDefault;
  private List mDefaultList;
  private TextView mFailed;
  private List mFailedList;
  private List mOkList;
  private SharedPreferences mSp;
  private TextView mSuccess;

  public Report()
  {
	  int[] arrayOfInt = { R.string.touchscreen_name, R.string.lcd_name, R.string.gps_name, R.string.battery_name, R.string.KeyCode_name,R.string.speaker_name, R.string.headset_name, R.string.microphone_name, R.string.earphone_name, R.string.wifi_name, R.string.bluetooth_name, R.string.vibrator_name, R.string.telephone_name, R.string.backlight_name, R.string.memory_name, R.string.gsensor_name, R.string.msensor_name, R.string.lsensor_name, R.string.psensor_name,  R.string.sdcard_name, R.string.camera_name,R.string.subcamera_name, R.string.fmradio_name, R.string.sim_name,R.string.led_name};
    
      this.itemString = arrayOfInt;
  }

  protected void ShowInfo()
  {
    String str1 = " | ";
    String str2 = "\n";
    StringBuilder localStringBuilder = new StringBuilder().append(getString(R.string.report_ok)).append(str2);
    int j = this.mOkList.size();
    for (int i = 0; i < j; i++)
    {
      localStringBuilder.append((String)this.mOkList.get(i)).append(str1);
    }
    this.mSuccess.setText(localStringBuilder.toString());
    StringBuilder localStringBuilder3 = new StringBuilder().append(str2).append(getString(2131230883)).append(str2);
    int l = this.mFailedList.size();
    for (int k = 0; k < l; k++)
    {
      localStringBuilder3.append((String)this.mFailedList.get(k)).append(str1);
    }
    this.mFailed.setText(localStringBuilder3.toString());
    StringBuilder localStringBuilder5 = new StringBuilder().append(str2).append(getString(2131230884)).append(str2);
    int i2 = this.mDefaultList.size();
    for (int i1 = 0; i1 < i2; i1++)
    {
      localStringBuilder5.append((String)this.mDefaultList.get(i1)).append(str1);
    }
    this.mDefault.setText(localStringBuilder5.toString());
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.report);
    SharedPreferences localSharedPreferences1 = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences1;
    TextView localTextView1 = (TextView)findViewById(R.id.report_success);
    this.mSuccess = localTextView1;
    TextView localTextView2 = (TextView)findViewById(R.id.report_failed);
    this.mFailed = localTextView2;
    TextView localTextView3 = (TextView)findViewById(R.id.report_default);
    this.mDefault = localTextView3;
    ArrayList localArrayList1 = new ArrayList();
    this.mOkList = localArrayList1;
    ArrayList localArrayList2 = new ArrayList();
    this.mFailedList = localArrayList2;
    ArrayList localArrayList3 = new ArrayList();
    this.mDefaultList = localArrayList3;
    
    for(int i = 0; i < this.itemString.length; i++){
  
        if(this.mSp.getString(getString(this.itemString[i]), null).equals("success"))
        {
            this.mOkList.add(getString(this.itemString[i]));
        }
        else if(this.mSp.getString(getString(this.itemString[i]), null).equals("failed"))
        {
            this.mFailedList.add(getString(this.itemString[i]));
        }
        else
        {
            this.mDefaultList.add(getString(this.itemString[i]));
        }
    }
    ShowInfo();
  }
}
