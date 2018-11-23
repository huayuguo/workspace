package com.mediatek.factorymode.fmradio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import android.widget.Toast;

public class FmTest2 extends Activity implements OnClickListener{

	private Button btnOk, btnFail;
	private SharedPreferences mSp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fm2);
		this.mSp = getSharedPreferences("FactoryMode", 0);
		btnOk = (Button) findViewById(R.id.bt_ok);
		btnFail = (Button)findViewById(R.id.bt_failed);
		
		btnOk.setOnClickListener(this);
		btnFail.setOnClickListener(this);
		
		try {
			Intent intent = new Intent();
			intent.setClassName("com.mediatek.FMRadio",
					"com.mediatek.FMRadio.FMRadioActivity");
			startActivityForResult(intent, 100);
		} catch (Exception e) {
			try {
				Intent intent = new Intent();
				intent.setClassName("com.mediatek.fmradio",
						"com.mediatek.fmradio.FmRadioActivity");
				startActivityForResult(intent, 100);
			} catch (Exception ex) {
				
				try {
					Intent intent = new Intent();
					intent.setClassName("com.android.fmradio",
							"com.android.fmradio.FmMainActivity");
					startActivityForResult(intent, 100);
				} catch (Exception ex2) {
										Toast.makeText(getApplicationContext(),
							"Not found FMRadio Application !!!", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_ok:
			setValue(true);
			break;
		case R.id.bt_failed:
			setValue(false);
			break;

		default:
			break;
		}
	}
	
	public void setValue(boolean value){
		 boolean bool = value;
	     Context localContext = getApplicationContext();
	     SharedPreferences localSharedPreferences = this.mSp;
	     if(bool){
	         Utils.SetPreferences(localContext, localSharedPreferences,R.string.fmradio_name, "success");
	         finish();
	     }else
	     {
	         Utils.SetPreferences(localContext, localSharedPreferences, R.string.fmradio_name, "failed");
	         finish();
	     }
	}

}
