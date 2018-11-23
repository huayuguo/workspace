package com.mediatek.factorymode.memory;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.Utils;
import java.io.IOException;

public class Memory extends Activity
  implements View.OnClickListener
{
  private TextView mBtFailed;
  private TextView mBtOk;
  private TextView mCommInfo;
  SharedPreferences mSp;

  private String getInfo(String paramString)
  {
      StringBuilder sb = new StringBuilder();
      long blockSize = 0;
      long blockCount = 0;
      long availCount = 0;
      long totalSize = 0;
      long availSize = 0;
 
     //StatFs rootsf = new StatFs(Environment.getRootDirectory().getPath()); 
     StatFs rootsf = new StatFs(Environment.getDataDirectory().getPath()); 
      blockSize = rootsf.getBlockSize();  
      blockCount = rootsf.getBlockCount();  
      availCount = rootsf.getAvailableBlocks(); 
      totalSize = blockCount * blockSize / 1024L / 1024L;
      availSize = availCount * blockSize / 1024L / 1024L;

      sb.append(this.getString(R.string.internal_memory)).append("\n\n");
      sb.append(this.getString(R.string.sdcard_totalsize)).append(totalSize).append("MB").append("\n\n");
      sb.append(this.getString(R.string.sdcard_freesize)).append(availSize).append("MB").append("\n\n");
      
      return sb.toString();
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    //int i = 2131230850; 	
    int j = paramView.getId();
    int k = this.mBtOk.getId();
    int kk = this.mBtFailed.getId();
    if (j == k)
    {
       Utils.SetPreferences(this, localSharedPreferences, R.string.memory_name, "success");
       finish();
    }
    if (j == kk)
    {
    	Utils.SetPreferences(this, localSharedPreferences, R.string.memory_name, "failed");
    	 finish();
    }
   
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.memory);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    TextView localTextView1 = (TextView)findViewById(R.id.bt_ok);
    this.mBtOk = localTextView1;
    this.mBtOk.setOnClickListener(this);
    TextView localTextView2 = (TextView)findViewById(R.id.bt_failed);
    this.mBtFailed = localTextView2;
    this.mBtFailed.setOnClickListener(this);
    TextView localTextView3 = (TextView)findViewById(R.id.comm_info);
    this.mCommInfo = localTextView3;
    TextView localTextView4 = this.mCommInfo;
    String str = getInfo("cat /proc/driver/nand");
    localTextView4.setText(str);
  }
}