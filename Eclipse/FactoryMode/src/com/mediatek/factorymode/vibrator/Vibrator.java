package com.mediatek.factorymode.vibrator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

/** add jyl 20150612 */
// @A: add import
import android.os.SystemProperties;
/* end */

public class Vibrator extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  private SharedPreferences mSp;
  private android.os.Vibrator mVibrator;

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.vibrator_name, "success");
        finish();
    }else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.vibrator_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2130903066);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
    this.mVibrator = (android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    
    /** add jyl 20150512 */
    // @M: custom the vibrator time
    if (SystemProperties.get("ro.yjzn.vibrator_test_cust", "").equals("yes")) {
        final int vtime = SystemProperties.getInt("ro.yjzn.vibrate_time", 1000);
        final int vInternal = SystemProperties.getInt("ro.yjzn.vibrate_internal", 1000);
        final int vrepeat = SystemProperties.getInt("ro.yjzn.vibrate_repeat", -1);
        long[] pattern = new long[] {vInternal,vtime};
        this.mVibrator.vibrate(pattern, vrepeat);
    } else {
        this.mVibrator.vibrate(1000L);
    }
    //this.mVibrator.vibrate(1000L);
    /* end */
  }

  public void onDestroy()
  {
    super.onDestroy();
    this.mVibrator.cancel();
  }
}