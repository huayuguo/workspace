package com.mediatek.factorymode.earphone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class Earphone extends Activity implements View.OnClickListener {
	private AudioManager mAudioManager;
	private Button mBtFailed;
	private Button mBtOk;
	private MediaPlayer mPlayer;
	private SharedPreferences mSp;

	private void initMediaPlayer() {
		MediaPlayer localMediaPlayer = MediaPlayer.create(
				getApplicationContext(), R.raw.cool);
		this.mPlayer = localMediaPlayer;
		this.mPlayer.setLooping(true);
		this.mPlayer.start();
	}

	public void onClick(View paramView) {
		SharedPreferences localSharedPreferences = this.mSp;
		if (paramView.getId() == this.mBtOk.getId()) {
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.earphone_name, "success");
			finish();
		} else {
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.earphone_name, "failed");
			finish();
		}

		/*
		 * int i = 2131230845; int j = paramView.getId(); int k =
		 * this.mBtOk.getId(); if (j == k); for (String str = "success"; ; str =
		 * "failed") { Utils.SetPreferences(this, localSharedPreferences, i,
		 * str); finish(); return; }
		 */
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.audio_test);
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences;
		Button localButton1 = (Button) findViewById(R.id.bt_ok);
		this.mBtOk = localButton1;
		this.mBtOk.setOnClickListener(this);
		Button localButton2 = (Button) findViewById(R.id.bt_failed);
		this.mBtFailed = localButton2;
		this.mBtFailed.setOnClickListener(this);
		this.mAudioManager = (AudioManager) getSystemService("audio");
		

	}

	@Override
	protected void onStop() {
		this.mPlayer.stop();
		this.mAudioManager.setMode(0);
		super.onStop();
		super.onStop();

	}

	protected void onResume() {
		super.onResume();
		// this.mAudioManager.setRingerMode(2);
		this.mAudioManager.setMode(AudioManager.MODE_IN_CALL);

		int i = this.mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, i, 0);
//		this.mAudioManager.setSpeakerphoneOn(false);
		// setVolumeControlStream(0);
		initMediaPlayer();
		/*
		Button localButton1 = (Button) findViewById(R.id.bt_ok);
		this.mBtOk = localButton1;
		this.mBtOk.setOnClickListener(this);
		Button localButton2 = (Button) findViewById(R.id.bt_failed);
		this.mBtFailed = localButton2;
		this.mBtFailed.setOnClickListener(this);
		*/
	}
}
