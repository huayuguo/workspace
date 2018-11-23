package com.mediatek.factorymode.microphone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.VUMeter;
import java.io.File;
import java.io.IOException;

public class MicRecorder extends Activity implements View.OnClickListener {
	Handler h;
	private Button mBtMicFailed;
	private Button mBtMicOk;
	private Button mBtSpkFailed;
	private Button mBtSpkOk;
	boolean mMicClick = false;
	private MediaPlayer mPlayer;
	private Button mRecord;
	private MediaRecorder mRecorder;
	SharedPreferences mSp;
	boolean mSpkClick = false;
	VUMeter mVUMeter;
	Runnable ra;

	class MicRecorder1 implements Runnable {
		public void run() {
			mVUMeter.invalidate();
			h.postDelayed(this, 100L);
		}
	}

	public MicRecorder() {
		this.h = new Handler();
		this.ra = new MicRecorder1();
	}

	private void start() {
		this.h.post(this.ra);
		if (this.mPlayer != null)
			this.mPlayer.stop();
		if (!Environment.getExternalStorageState().equals("mounted"))
			this.mRecord.setText(R.string.sdcard_tips_failed);
		try {
			this.mRecorder = new MediaRecorder();
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
			this.mRecord.setTag("ing");
		}

		/*
		 * Handler localHandler = this.h; Runnable localRunnable = this.ra;
		 * localHandler.post(localRunnable); if (this.mPlayer != null)
		 * this.mPlayer.stop(); if
		 * (!Environment.getExternalStorageState().equals("mounted"))
		 * this.mRecord.setText(2131230866); while (true) { return; try {
		 * MediaRecorder localMediaRecorder1 = new MediaRecorder();
		 * this.mRecorder = localMediaRecorder1;
		 * this.mRecorder.setAudioSource(1); this.mRecorder.setOutputFormat(1);
		 * this.mRecorder.setAudioEncoder(3); VUMeter localVUMeter =
		 * this.mVUMeter; MediaRecorder localMediaRecorder2 = this.mRecorder;
		 * localVUMeter.setRecorder(localMediaRecorder2); StringBuilder
		 * localStringBuilder1 = new StringBuilder(); File localFile =
		 * Environment.getExternalStorageDirectory(); StringBuilder
		 * localStringBuilder2 = localStringBuilder1.append(localFile); String
		 * str1 = File.separator; String str2 = str1 + "test.mp3"; if (!new
		 * File(str2).exists()) new File(str2).createNewFile();
		 * this.mRecorder.setOutputFile(str2); this.mRecorder.prepare();
		 * this.mRecorder.start(); label203: this.mRecord.setTag("ing");
		 * this.mRecord.setText(2131230803); } catch (Exception localException)
		 * { String str3 = localException.getMessage(); Toast.makeText(this,
		 * str3, 0); break label203: } }
		 */
	}

	private void stopAndSave() {
		this.h.removeCallbacks(this.ra);
		this.mRecorder.stop();
		this.mRecorder.release();
		this.mRecorder = null;
		this.mRecord.setText(R.string.Mic_start);
		this.mRecord.setTag("");
		this.mVUMeter.SetCurrentAngle(0);
		try {
			MediaPlayer localMediaPlayer = new MediaPlayer();
			this.mPlayer = localMediaPlayer;
			this.mPlayer.setDataSource("/sdcard/test.mp3");
			this.mPlayer.prepare();
			this.mPlayer.start();
		} catch (IllegalArgumentException localIllegalArgumentException) {
			localIllegalArgumentException.printStackTrace();
		} catch (IllegalStateException localIllegalStateException) {
			localIllegalStateException.printStackTrace();
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}

	public void isFinish() {
		if ((this.mMicClick != true) || (this.mSpkClick != true))
			return;
		finish();
	}

	public void onClick(View paramView) {
		SharedPreferences localSharedPreferences = this.mSp;
		String str1 = Environment.getExternalStorageState();
		String str2 = "mounted";

		if (paramView.getId() == this.mRecord.getId()) {
			if (str1.equals(str2)) {
				if ((this.mRecord.getTag() != null)
						&& (this.mRecord.getTag().equals("ing")))
					stopAndSave();
				else
					start();
			} else {
				this.mRecord.setText(R.string.sdcard_tips_failed);
			}
			return;
		}
		/*jacky add start*/
		switch (paramView.getId()) {
		case R.id.bt_ok:
			Utils.SetPreferences(this, localSharedPreferences, R.string.microphone_name, "success");
			finish();
			break;
		case R.id.bt_failed:
			Utils.SetPreferences(this, localSharedPreferences, R.string.microphone_name, "failed");
			finish();
			break;

		default:
			break;
		}
		/*end*/
		/*
		 * jacky delete if(paramView.getId() == this.mBtMicOk.getId()) {
		 * this.mMicClick = true;
		 * this.mBtMicFailed.setBackgroundColor(getResources
		 * ().getColor(R.color.gray));
		 * this.mBtMicOk.setBackgroundColor(getResources
		 * ().getColor(R.color.Green)); Utils.SetPreferences(this,
		 * localSharedPreferences, R.string.microphone_name, "success"); }
		 * 
		 * if(paramView.getId() == this.mBtMicFailed.getId()) { this.mMicClick =
		 * true;
		 * this.mBtMicFailed.setBackgroundColor(getResources().getColor(R.color
		 * .Red));
		 * this.mBtMicOk.setBackgroundColor(getResources().getColor(R.color
		 * .gray)); Utils.SetPreferences(this, localSharedPreferences,
		 * R.string.microphone_name, "failed"); } if(paramView.getId() ==
		 * this.mBtSpkOk.getId()) { this.mSpkClick = true;
		 * this.mBtSpkOk.setBackgroundColor
		 * (getResources().getColor(R.color.Green));
		 * this.mBtSpkFailed.setBackgroundColor
		 * (getResources().getColor(R.color.gray)); Utils.SetPreferences(this,
		 * localSharedPreferences, R.string.microphone_name, "success"); }
		 * if(paramView.getId() == this.mBtSpkFailed.getId()) { this.mSpkClick =
		 * true;
		 * this.mBtSpkFailed.setBackgroundColor(getResources().getColor(R.color
		 * .Red));
		 * this.mBtSpkOk.setBackgroundColor(getResources().getColor(R.color
		 * .gray)); Utils.SetPreferences(this, localSharedPreferences,
		 * R.string.microphone_name, "failed"); } isFinish();
		 */
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.micrecorder);
		this.mSp = getSharedPreferences("FactoryMode", 0);
		this.mRecord = (Button) findViewById(R.id.mic_bt_start);
		this.mRecord.setOnClickListener(this);
		/*
		 * jacky delete this.mBtMicOk = (Button)findViewById(R.id.mic_bt_ok);
		 * this.mBtMicOk.setOnClickListener(this); this.mBtMicFailed =
		 * (Button)findViewById(R.id.mic_bt_failed);
		 * this.mBtMicFailed.setOnClickListener(this); this.mBtSpkOk =
		 * (Button)findViewById(R.id.speaker_bt_ok);
		 * this.mBtSpkOk.setOnClickListener(this); this.mBtSpkFailed =
		 * (Button)findViewById(R.id.speaker_bt_failed);
		 * this.mBtSpkFailed.setOnClickListener(this);
		 */
		this.mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		/* jacky add start */
		findViewById(R.id.bt_ok).setOnClickListener(this);
		findViewById(R.id.bt_failed).setOnClickListener(this);
		/* end */
	}

	protected void onDestroy() {
		super.onDestroy();
		new File("/sdcard/test.mp3").delete();
		if (this.mPlayer != null)
			this.mPlayer.stop();
		if (this.mRecorder != null)
			this.mRecorder.stop();
	}
}