package com.mediatek.factorymode.backlight;

import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.Utils;

public class BackLight extends Activity
  implements View.OnClickListener
{
  private final int ERR_ERR = 1;
  private final int ERR_OK = 0;
  private String lcdCmdOFF = "echo 10 > /sys/class/leds/lcd-backlight/brightness";
  private String lcdCmdON = "echo 255 > /sys/class/leds/lcd-backlight/brightness";
  private Button mBtFailed;
  private Button mBtOk;
  private Button mBtnLcdOFF;
  private Button mBtnLcdON;
  private SharedPreferences mSp;

  private float mOldBrightness ;
  private float mStartBrightness;
  
  private void setLastError(int paramInt)
  {
    System.out.print(paramInt);
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    String[] arrayOfString = new String[3];
    arrayOfString[0] = "/system/bin/sh";
    arrayOfString[1] = "-c";
      
    if(paramView.getId() == this.mBtnLcdON.getId() 
            || paramView.getId() == this.mBtnLcdOFF.getId()){
    	/*add by Jacky*/
    	if(true){
    		WindowManager.LayoutParams lp=getWindow().getAttributes();
            mOldBrightness = lp.screenBrightness;
    		if(paramView.getId() == this.mBtnLcdON.getId()){
        		lp.screenBrightness=1.0f;
        	}else if(paramView.getId() == this.mBtnLcdOFF.getId()){
                lp.screenBrightness=0.1f;
                
        	}	
    		getWindow().setAttributes(lp);
    		mBtFailed.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					WindowManager.LayoutParams lp=getWindow().getAttributes();
		            lp.screenBrightness = mOldBrightness;
		            getWindow().setAttributes(lp);
				}
			},1500);
    		return;
    	}
    	
    	
    	/*end by Jacky*/
    	
        if(paramView.getId() == this.mBtnLcdON.getId())
        {
            arrayOfString[2] = this.lcdCmdON;
        }else if(paramView.getId() == this.mBtnLcdOFF.getId()){
            arrayOfString[2] = this.lcdCmdOFF;
        }
        
        try {
                if(ShellExe.execCommand(arrayOfString) == 0)
                    setLastError(0);
                	                else
                   setLastError(1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            setLastError(1);
        }
    }

    if (paramView.getId() == this.mBtOk.getId())
    {
        Utils.SetPreferences(this, localSharedPreferences, R.string.backlight_name, "success");
        finish();
    }
    else if(paramView.getId() == this.mBtFailed.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.backlight_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.backlight);
    
    /*jacky add start*/
    WindowManager.LayoutParams lp=getWindow().getAttributes();
    mStartBrightness = lp.screenBrightness;
    lp.screenBrightness = 0.5f;
    getWindow().setAttributes(lp);
    
    /*end*/
    
    this.mBtnLcdON = (Button)findViewById(R.id.Display_lcd_on);
    this.mBtnLcdOFF =(Button)findViewById(R.id.Display_lcd_off);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtnLcdON.setOnClickListener(this);
    this.mBtnLcdOFF.setOnClickListener(this);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed.setOnClickListener(this);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
  }
  @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		   /*jacky add start*/
	    WindowManager.LayoutParams lp=getWindow().getAttributes();
	    lp.screenBrightness = mStartBrightness;
	    getWindow().setAttributes(lp);
	    
	    /*end*/
  }
}