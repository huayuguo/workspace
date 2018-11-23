package com.mediatek.factorymode.fmradio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;


import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class FMRadio extends Activity
{
  static Cursor mCursor = null;
  SharedPreferences mSp;

  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
	  SharedPreferences localSharedPreferences = this.mSp;
	  if(paramInt2 == -1)
		  Utils.SetPreferences(this, localSharedPreferences,R.string.fmradio_name, "success");
	  else
		  Utils.SetPreferences(this, localSharedPreferences,R.string.fmradio_name, "failed");
	  finish();
	  /* if(paramInt2 == -1){ //ok
        boolean bool = paramIntent.getBooleanExtra("result", false);
          Context localContext = getApplicationContext();
          SharedPreferences localSharedPreferences = this.mSp;
          if(bool){
              Utils.SetPreferences(localContext, localSharedPreferences,R.string.fmradio_name, "success");
              finish();
          }else
          {
              Utils.SetPreferences(localContext, localSharedPreferences, R.string.fmradio_name, "failed");
              finish();
          }
      }else{
          finish();
      }*/
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.mSp = getSharedPreferences("FactoryMode", 0);
   // Intent intent = new Intent();
  //  intent.setClass(this, com.mediatek.factorymode.simcard.SimCard.class);
  //  startActivity(intent);
    //Intent localIntent = new Intent("com.mediatek.FMRadio.FMRadioActivity");
    //Intent localIntent = new Intent("com.mediatek.factorymode.simcard.SimCard");
    //startActivityForResult(localIntent, 3);
    //startActivity(localIntent);
    Intent localIntent = new Intent("com.mediatek.FMRadio.test");
    startActivityForResult(localIntent, 3);
  }
}
