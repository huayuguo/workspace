package com.mediatek.factorymode.microphone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class Microphone extends Activity
  implements View.OnClickListener
{
  private Button btfailed;
  private Button btok;
  public Handler h;
  RecordThread rt;
  private SharedPreferences sp;
  private TextView tvstatus;

  class Microphone1 extends Handler
  {
    public void handleMessage(Message paramMessage)
    {
      super.handleMessage(paramMessage);
      String str = String.valueOf(paramMessage.arg1);
      tvstatus.setText(str);
    }
  }
  
  public Microphone()
  {
      Microphone1 local1 = new Microphone1();
      this.h = local1;
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.sp;
    if(paramView.getId() == this.btok.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.Microphone, "success");
        finish();
    }else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.Microphone, "failed");
        finish();
    }
    /*int i = 2131230801;
    int j = paramView.getId();
    int k = this.btok.getId();
    if (j == k);
    for (String str = "success"; ; str = "failed")
    {
      Utils.SetPreferences(this, localSharedPreferences, i, str);
      finish();
      return;
    }*/
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.microphone);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.sp = localSharedPreferences;
    this.tvstatus = (TextView)findViewById(R.id.mic_tv_status);
    this.btok = (Button)findViewById(R.id.bt_ok);
    this.btok.setOnClickListener(this);
    this.btfailed = (Button)findViewById(R.id.bt_failed);
    this.btfailed.setOnClickListener(this);
    this.rt = new RecordThread();
  }

  public void onDestroy()
  {
    super.onDestroy();
    this.rt.destroy();
  }

  public void onPause()
  {
    super.onPause();
    this.rt.pause();
  }

  public void onResume()
  {
    super.onResume();
    this.rt.start();
  }

  public class RecordThread extends Thread
  {
    private static final int SAMPLE_RATE_IN_HZ = 44100;
    private AudioRecord ar;
    private int bs;
    private boolean isRun = false;

    public RecordThread()
    {
       this.bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, 2, 2);
       AudioRecord localAudioRecord = new AudioRecord(1, SAMPLE_RATE_IN_HZ, 2, 2, this.bs);
       this.ar = localAudioRecord;
    }

    public void pause()
    {
      this.isRun = false;
    }

    public void run()
    {
      super.run();
      this.ar.startRecording();
      byte[] arrayOfByte = new byte[this.bs];
      AudioRecord localAudioRecord = this.ar;
      int i = this.bs;
      int j = localAudioRecord.read(arrayOfByte, 0, i);
      this.isRun = true;
      while (this.isRun)
      {
        int localObject1 = 0;
        int localObject2 = 0;
        int k = 0;
        while (true)
        {
          int l = arrayOfByte.length;
          if (localObject2 >= l)
            break;
          int i1 = arrayOfByte[localObject2];
          int i2 = arrayOfByte[localObject2];
          int i3 = i1 * i2;
          k = localObject1 + i3;
          ++localObject2;
        }
        Message localMessage = new Message();
        float f1 = k;
        float f2 = j;
        int i4 = (int)(f1 / f2);
        localMessage.arg1 = i4;
        Microphone.this.h.sendMessage(localMessage);
      }
      this.ar.stop();
    }

    public void start()
    {
      if (this.isRun)
        return;
      super.start();
    }
  }
}
