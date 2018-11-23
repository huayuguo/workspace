package com.mediatek.factorymode.sensor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import android.util.Log;

public class GSensorCali extends Activity implements View.OnClickListener
{
	private Button btnOk,btnFail;
	private SharedPreferences mSp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsensorcali);
		mSp = getSharedPreferences(
				"FactoryMode", 0);
		
		btnOk = (Button) findViewById(R.id.bt_ok);
		btnFail = (Button)findViewById(R.id.bt_failed);
		
		btnOk.setOnClickListener(this);
		btnFail.setOnClickListener(this);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.putExtra("testmode", true);
		intent.setComponent(new ComponentName("com.yjzn.gsensorcali", "com.yjzn.gsensorcali.MainActivity"));
		startActivityForResult(intent,109);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 109 && data != null){
			int ret = data.getIntExtra("kcali_status", -1);
			if(ret == 1){//success
				setResult(R.string.gsensorcali_name, R.id.bt_ok)  ;
			}else if (resultCode == 0){ //fail
				setResult(R.string.gsensorcali_name, R.id.bt_failed)  ;
			}
			finish();
			
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_failed:
		case R.id.bt_ok:
			setResult(R.string.gsensorcali_name,v.getId());
			
			break;
		default:
			break;
		}
	}
	
	private void setResult(int key, int id){
		SharedPreferences localSharedPreferences = this.mSp;
		if (id == this.btnOk.getId()) {
			Utils.SetPreferences(this, localSharedPreferences, key, "success");
			finish();
		} else {
			Utils.SetPreferences(this, localSharedPreferences, key, "failed");
			finish();
		}
	}
}
