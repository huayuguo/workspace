package com.mediatek.factorymode.version;

import com.mediatek.factorymode.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.mediatek.factorymode.Utils;
import android.os.SystemProperties;

public class version extends Activity implements OnClickListener {
	private TextView mInfo;
	private SharedPreferences mSp;

	private TextView mBtFailed;
	private TextView mBtOk;

	public void onClick(View paramView) {
		SharedPreferences localSharedPreferences = this.mSp;
		// int i = 2131230850;
		int j = paramView.getId();
		int k = this.mBtOk.getId();
		int kk = this.mBtFailed.getId();
		if (j == k) {
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.version, "success");
			finish();
		}
		if (j == kk) {
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.version, "failed");
			finish();
		}

	}

	public void VersionTest() {
		StringBuilder localStringBuilder = new StringBuilder();
		// String custom_verno = SystemProperties.get("ro.build.verno");
		String custom_verno = SystemProperties.get("ro.custom.build.version");
		// String display_id = SystemProperties.get("ro.build.display.id");
		String display_id = SystemProperties.get("ro.build.display.id");// modify
																		// by
																		// Jacky
		String value = SystemProperties.get("ro.build.date");
		String code = getResources().getString(R.string.barcode_fail);
		String barcode = SystemProperties.get("gsm.serial");
		if(barcode != null){
			try {
				String[] words = barcode.split("\\s+");
				if(words!=null && words.length == 2 && words[1].trim().equals("10")){
					code = getResources().getString(R.string.barcode_fuck_ok);
				}else if(barcode.trim().equals("10")){
					code = getResources().getString(R.string.barcode_fuck_ok);
				}else if(words!=null && words.length == 2 && words[1].trim().equals("10P")){
					code = getResources().getString(R.string.barcode_ok);
				}else if(barcode.trim().equals("10P")){
					code = getResources().getString(R.string.barcode_ok);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		localStringBuilder.append(custom_verno).append("\n").append(display_id)
				.append("\n").append(value).append("\n").append(code);// S006-SSD-P4-FM-ZH
		mInfo.setText(localStringBuilder.toString());
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.version);
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences;
		TextView localTextView = (TextView) findViewById(R.id.version_info);
		mInfo = localTextView;

		TextView localTextView1 = (TextView) findViewById(R.id.bt_ok);
		this.mBtOk = localTextView1;
		this.mBtOk.setOnClickListener(this);
		TextView localTextView2 = (TextView) findViewById(R.id.bt_failed);
		this.mBtFailed = localTextView2;
		this.mBtFailed.setOnClickListener(this);

		VersionTest();
	}
}
