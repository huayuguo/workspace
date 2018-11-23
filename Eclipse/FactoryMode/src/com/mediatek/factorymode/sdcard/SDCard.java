package com.mediatek.factorymode.sdcard;

import java.lang.reflect.Method;

import com.mediatek.factorymode.R;
import java.io.File;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.mediatek.factorymode.Utils;
import android.util.Log;

public class SDCard extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  private TextView mInfo;
  private SharedPreferences mSp;

    public String getSDcardPath() {
    try {
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
        String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
// second element in paths[] is secondary storage path
        return paths.length <= 1 ? null : paths[1];
    } catch (Exception e) {
        Log.e("Keven","e =="+e);
    }
        return null;
    }

    public String getStorageState(String path) {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", new Class[] {String.class});
            String state = (String) getVolumeStateMethod.invoke(sm, path);
            return state;
        } catch (Exception e) {
            Log.e("Keven","e =="+e);
        }
        return null;
    }

  public void SDCardSizeTest()
  {
      StringBuilder localStringBuilder = new StringBuilder();
      try{ // tongjun  Add
          //StatFs localStatFs = new StatFs("/mnt/m_external_sd");///mnt/sdcard
		  String path = getSDcardPath();//Environment.getExternalStorageDirectory().getPath();
		  Log.e("Keven","path =="+path);
          StatFs localStatFs = new StatFs(path);///mnt/sdcard
          long l1 = localStatFs.getBlockCount();
          if(l1 == 0){
              localStringBuilder.append(this.getString(R.string.sdcard_tips_failed));
          }else{
              long l2 = localStatFs.getBlockSize();
              long l3 = localStatFs.getAvailableBlocks();
              long l4 = l1 * l2 / 1024L / 1024L;
              long l5 = l3 * l2 / 1024L / 1024L;
              localStringBuilder.append(this.getString(R.string.sdcard_tips_success)).append("\n\n");
              localStringBuilder.append(this.getString(R.string.sdcard_totalsize)).append(l4).append("MB").append("\n\n");
              localStringBuilder.append(this.getString(R.string.sdcard_freesize)).append(l5).append("MB").append("\n\n");
          }
	  }catch(Exception e)  // tongjun  @{
	  {
		 localStringBuilder.append(this.getString(R.string.sdcard_tips_failed));
       }					// tongjun @}
      this.mInfo.setText(localStringBuilder.toString());
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.sdcard_name, "success");
        finish();
    }
    else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.sdcard_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.sdcard);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    TextView localTextView = (TextView)findViewById(R.id.sdcard_info);
    this.mInfo = localTextView;
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
    SDCardSizeTest();
  }
}