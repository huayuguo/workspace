package com.mediatek.factorymode.lcd;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import java.util.Timer;
import com.mediatek.factorymode.R;
import android.os.Message;
import java.util.TimerTask;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.mediatek.factorymode.Utils;
import android.content.Context;
import android.app.AlertDialog;
import android.graphics.Color;

public class LCD extends Activity
{
  private int mNum = 0;
  SharedPreferences mSp;
  private TextView mText1 = null;
  Handler myHandler;
  private Timer timer;

  class LCD12
  implements DialogInterface.OnClickListener
{
  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    Context localContext = getApplicationContext();
    SharedPreferences localSharedPreferences = mSp;
    Utils.SetPreferences(localContext, localSharedPreferences, R.string.lcd_name, "failed");
    finish();
  }
}
  
  class LCD11
  implements DialogInterface.OnClickListener
{
  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    Context localContext = getApplicationContext();
    SharedPreferences localSharedPreferences = mSp;
    Utils.SetPreferences(localContext, localSharedPreferences, R.string.lcd_name, "success");
    finish();
  }
}
  
  class LCD2 extends TimerTask
  {
    public void run()
    {
      Message localMessage = new Message();
      localMessage.what = 0;
      myHandler.sendMessage(localMessage);
    }
  } 
  class LCD1 extends Handler
  {
	
    public void handleMessage(Message paramMessage)
    {
      if (paramMessage.what == 0&&(mNum >3))
      {
        timer.cancel();
        myHandler.removeMessages(0);
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(LCD.this);
        localBuilder.setTitle(R.string.FMRadio_notice);
        LCD11 local1 = new LCD11();
        localBuilder.setPositiveButton(R.string.Success, local1);
        String str = getResources().getString(R.string.Failed);
        LCD12 local2 = new LCD12();
        localBuilder.setNegativeButton(str, local2);
        localBuilder.create().show();
        return;
      }
      mNum ++;    
      changeColor(mNum);
    }
  }
  
  public LCD()
  {
    LCD1 local1 = new LCD1();
    this.myHandler = local1;
  }

private void changeColor(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
    	this.mText1.setBackgroundColor(Color.RED);
    	break;
    case 1:
    	this.mText1.setBackgroundColor(Color.BLUE);
    	break;
    case 2:
    	this.mText1.setBackgroundColor(Color.GREEN);
    	break;
    case 3:
    	this.mText1.setBackgroundColor(Color.WHITE);
    	break;
    }
  
  }

  private void initView()
  {
    TextView localTextView = (TextView)findViewById(R.id.test_color_text1);
    this.mText1 = localTextView;
    Timer localTimer = this.timer;
    LCD2 local2 = new LCD2();
    long l = 1000L;
    localTimer.schedule(local2, 1000L, l);
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    setContentView(R.layout.lcd);
    Timer localTimer = new Timer();
    this.timer = localTimer;
    initView();
  }

  protected void onDestroy()
  {
    super.onDestroy();
    this.timer.cancel();
  }
}