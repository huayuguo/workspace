package com.mediatek.factorymode.headset;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.VUMeter;
import java.io.File;
import java.io.IOException;

public class HeadSet extends Activity implements View.OnClickListener {
	private static final int STATE_HEADSET_PLUG = 0;
	private static final int STATE_HEADSET_UNPLUG = 1;
	Handler h;
	private Button mBtFailed;
	private Button mBtOk;
	boolean mMicClick = false;
	private MediaPlayer mPlayer = null;
	BroadcastReceiver mReceiver;
	private Button mRecord;
	private MediaRecorder mRecorder = null;
	SharedPreferences mSp;
	boolean mSpkClick = false;
	VUMeter mVUMeter;
	Handler myHandler;
	Runnable ra;

	class HeadSet1 implements Runnable {
		public void run() {
			mVUMeter.invalidate();
			h.postDelayed(this, 100L);
		}
	}

	class HeadSet2 extends Handler {
		public void handleMessage(Message paramMessage) {
			MediaRecorder localMediaRecorder = null;
			boolean bool = true;
			super.handleMessage(paramMessage);

			switch (paramMessage.what) {
			// default:
			case 0:
				mRecord.setText(R.string.HeadSet_tips);
				mRecord.setEnabled(false);
				break;
			case 1:
				String str1 = Environment.getExternalStorageState();
				String str2 = "mounted";
				if (str1.equals(str2)) {
					mRecord.setText(R.string.Mic_start);
					mRecord.setEnabled(true);

				} else {
					mRecord.setText(R.string.sdcard_tips_failed);
					mRecord.setEnabled(false);
				}

				break;
			}

		}
	}

	class HeadSet3 extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {
			String str = paramIntent.getAction();
            int mic = paramIntent.getIntExtra("microphone", -1);
			if ("android.intent.action.HEADSET_PLUG".equals(str)) {
				if (paramIntent.getIntExtra("state", 0) == 1) {
					myHandler.sendEmptyMessage(1);
				} else{
                    if(mic ==1)  {
                        myHandler.sendEmptyMessage(0);
                    }
                }
			}

		}
	}

	public HeadSet() {
		Handler localHandler = new Handler();
		this.h = localHandler;
		HeadSet1 local1 = new HeadSet1();
		this.ra = local1;
		HeadSet2 local2 = new HeadSet2();
		this.myHandler = local2;
		HeadSet3 local3 = new HeadSet3();
		this.mReceiver = local3;
	}

	private void start() {
		Handler localHandler = this.h;
		Runnable localRunnable = this.ra;
		localHandler.post(localRunnable);
		if (this.mPlayer != null)
			this.mPlayer.stop();
		if (!Environment.getExternalStorageState().equals("mounted"))
			this.mRecord.setText(2131230866);

		try {
			MediaRecorder localMediaRecorder1 = new MediaRecorder();
			this.mRecorder = localMediaRecorder1;
			this.mRecorder.setAudioSource(1);
			this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mRecorder.setAudioEncodingBitRate(16);
            this.mRecorder.setAudioSamplingRate(44100); 
			this.mVUMeter.setRecorder(this.mRecorder);
			StringBuilder localStringBuilder = new StringBuilder();
			String str = null;
			File localFile = Environment.getExternalStorageDirectory();
			localStringBuilder.append(localFile).append(File.separator)
					.append("test.mp3");
			str = localStringBuilder.toString();
			if (!new File(str).exists())
				new File(str).createNewFile();
			this.mRecorder.setOutputFile(str);
			this.mRecorder.prepare();
			this.mRecorder.start();
			this.mRecord.setTag("ing");
			this.mRecord.setText(R.string.Mic_stop);

		} catch (Exception localException) {
			String str3 = localException.getMessage();
			Toast.makeText(this, str3, 0);
			// break;
		}

	}

	private void stopAndSave() {
		Handler localHandler = this.h;
		Runnable localRunnable = this.ra;
		localHandler.removeCallbacks(localRunnable);
		this.mRecord.setText(R.string.Mic_start);
		this.mRecord.setTag("");
		this.mVUMeter.SetCurrentAngle(0);
		this.mRecorder.stop();
		this.mRecorder.release();
		this.mRecorder = null;
		this.mVUMeter.setRecorder(null);//add by Jacky
		try {
			MediaPlayer localMediaPlayer = new MediaPlayer();
			this.mPlayer = localMediaPlayer;
			this.mPlayer.setAudioStreamType(3);
			this.mPlayer.setDataSource("/sdcard/test.mp3");
			this.mPlayer.prepare();
			this.mPlayer.start();
			return;
		} catch (IllegalArgumentException localIllegalArgumentException) {
			localIllegalArgumentException.printStackTrace();
		} catch (IllegalStateException localIllegalStateException) {
			localIllegalStateException.printStackTrace();
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}

	public void onClick(View paramView) {
		int i = R.string.headset_name;
		int j = paramView.getId();
		int k = this.mRecord.getId();
		if (j == k) {
			if ((this.mRecord.getTag() != null)
					&& (this.mRecord.getTag().equals("ing")))
				stopAndSave();
			else
				start();
			return;
		}
		int l = paramView.getId();
		int i1 = this.mBtOk.getId();
		if (l == i1) {
			SharedPreferences localSharedPreferences1 = this.mSp;
			Utils.SetPreferences(this, localSharedPreferences1, i, "success");
			finish();
			return;
		}
		int i2 = paramView.getId();
		int i3 = this.mBtFailed.getId();
		if (i2 == i3) {
			SharedPreferences localSharedPreferences1 = this.mSp;
			Utils.SetPreferences(this, localSharedPreferences1, i, "failed");
			finish();
			return;
		}
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.headset);
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences;
		Button localButton1 = (Button) findViewById(R.id.mic_bt_start);
		this.mRecord = localButton1;
		this.mRecord.setOnClickListener(this);
		this.mRecord.setEnabled(false);
		Button localButton2 = (Button) findViewById(R.id.bt_ok);
		this.mBtOk = localButton2;
		this.mBtOk.setOnClickListener(this);
		Button localButton3 = (Button) findViewById(R.id.bt_failed);
		this.mBtFailed = localButton3;
		this.mBtFailed.setOnClickListener(this);
		VUMeter localVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		this.mVUMeter = localVUMeter;
		IntentFilter localIntentFilter = new IntentFilter(
				"android.intent.action.HEADSET_PLUG");
		localIntentFilter.setPriority(999);
		BroadcastReceiver localBroadcastReceiver = this.mReceiver;
		registerReceiver(localBroadcastReceiver, localIntentFilter);
	}

	protected void onDestroy() {
		super.onDestroy();
		new File("/sdcard/test.mp3").delete();
		if (this.mPlayer != null)
			this.mPlayer.stop();
		if (this.mRecorder != null)
			this.mRecorder.stop();
		BroadcastReceiver localBroadcastReceiver = this.mReceiver;
		unregisterReceiver(localBroadcastReceiver);
		Handler localHandler = this.h;
		Runnable localRunnable = this.ra;
		localHandler.removeCallbacks(localRunnable);
	}
}