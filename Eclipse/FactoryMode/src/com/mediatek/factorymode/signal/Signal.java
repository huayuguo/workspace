package com.mediatek.factorymode.signal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class Signal extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  SharedPreferences mSp;

  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
    localBuilder.setTitle(R.string.FMRadio_notice);
    localBuilder.setMessage(R.string.HeadSet_hook_message);
    localBuilder.setPositiveButton(R.string.Success, new DialogInterface.OnClickListener(){
        public void onClick(DialogInterface dialog, int which) {
            Utils.SetPreferences(Signal.this, Signal.this.mSp, R.string.headsethook_name, "success");
        }
    });
    localBuilder.setNegativeButton(R.string.Failed, new DialogInterface.OnClickListener(){
        public void onClick(DialogInterface dialog, int which) {
            Utils.SetPreferences(Signal.this, Signal.this.mSp, R.string.headsethook_name, "failed");
        }
        
    });
    //del by Jacky localBuilder.create().show();
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.telephone_name, "success");
        finish();
    }
    else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.telephone_name, "failed");
        finish();
    }
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);

    setContentView(R.layout.signal);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
    Uri localUri = Uri.fromParts("tel", "112", null);
    Intent localIntent = new Intent("android.intent.action.CALL_PRIVILEGED", localUri);
    localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); 
    startActivityForResult(localIntent, 5);

  }
}
