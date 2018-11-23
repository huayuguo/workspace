package com.mediatek.factorymode.audio;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class AudioTest extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  private MediaPlayer mPlayer;
  private SharedPreferences mSp;

  private void initMediaPlayer()
  {
    MediaPlayer localMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tada);
    this.mPlayer = localMediaPlayer;
    this.mPlayer.setLooping(true);
    this.mPlayer.start();
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId())
    {
        Utils.SetPreferences(this, localSharedPreferences, R.string.speaker_name, "success");
        finish();
    }else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.speaker_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.audio_test);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
    AudioManager localAudioManager = (AudioManager)getSystemService("audio");
    localAudioManager.setRingerMode(2);
    int i = localAudioManager.getStreamMaxVolume(3);
    localAudioManager.setStreamVolume(3, i, 4);
    initMediaPlayer();
  }

  protected void onDestroy()
  {
    super.onDestroy();
    this.mPlayer.stop();
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if ((paramInt == 4) && (paramKeyEvent.getRepeatCount() == 0))
      finish();
    return true;
  }

  protected void onResume()
  {
    super.onResume();
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
  }
}