package com.mediatek.factorymode.led;

import android.app.Activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class Led extends Activity implements View.OnClickListener, Runnable
{
  private Button mBtFailed;
  private Button mBtOk;
  private SharedPreferences mSp;
  private NotificationManager nM;
  private Notification n;
  private int ledColor = 0x00ff0000;//start red;
  private boolean bExit = false;

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.led_name, "success");
        bExit = true;
	cancellLedShow();
        finish();
    }else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.led_name, "failed");
        bExit = true;
	cancellLedShow();
        finish();
        
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.vibrator);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mBtOk = (Button)findViewById(R.id.vibrator_bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.vibrator_bt_failed);
    this.mBtFailed.setOnClickListener(this);
    nM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    n = new Notification();
    n.flags |= Notification.FLAG_SHOW_LIGHTS;
    n.ledOnMS = 1000;
    n.ledOffMS = 0;
    new Thread(this).start();
    
  }
  
  public void onDestroy()
  {
    super.onDestroy();
    bExit = true;
    cancellLedShow();
  }
  
  public void updateLedState(){
      n.ledARGB = 0xff000000|ledColor;
      Log.i("LED", "argb="+n.ledARGB);
      nM.notify(1, n);

      ledColor = ledColor>>8;
      if(ledColor == 0)ledColor = 0x00ff0000;
  }
  
  private void cancellLedShow(){
      nM.cancel(1);
  }

    public void run() {
        while(!Thread.currentThread().isInterrupted() && (!bExit)){
	    //change led
            updateLedState();
            //sleep 1 s
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //cancell
            cancellLedShow();
            
        }
        
        
    }
}
