package com.mediatek.factorymode.simcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.factorymode.Utils;

import com.mediatek.factorymode.R; //zqf 
import android.content.SharedPreferences;
/**
 * SIM card test, only support L
 * @author ivan
 *
 */
public class SimTest extends Activity implements OnClickListener {
	private final static String TAG = "SimTest";

	protected static int mSimTestStatus = 0;
	private Button btSuccess;
	private Button btFail;
	private TextView sim1_state;
	private TextView sim2_state;

	private SharedPreferences mSp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_result_caption_sim);
		getWindow().addFlags(128);
		
		btSuccess = (Button) findViewById(R.id.status_success);
		btSuccess.setOnClickListener(this);
		btFail = (Button) findViewById(R.id.status_fail);
		btFail.setOnClickListener(this);
		sim1_state = (TextView) findViewById(R.id.sim1_state);
		sim2_state = (TextView) findViewById(R.id.sim2_state);
		
		simCardTest();
	}
	
	@Override
	public void onBackPressed() {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSp = getSharedPreferences("FactoryMode", 0);
		mSimTestStatus = 0;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void simCardTest() {
		//KK
		/*
		TelephonyManagerEx teleEx = TelephonyManagerEx.getDefault();
        boolean isSim1StateReady = (teleEx.getSimState(PhoneConstants.GEMINI_SIM_1) == SIM_STATE_READY);
        boolean isSim2StateReady = (teleEx.getSimState(PhoneConstants.GEMINI_SIM_2) == SIM_STATE_READY);
        */
		
        //L
		TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final int count = telMgr.getSimCount();
		Log.i(TAG, "simcard count = " + count);
		
		int state1 = -1;
		int state2 = -1;
		if(count > 0) {
			state1 = telMgr.getSimState(0);
			Log.e(TAG, "state1 = " + state1);
			if(state1 == TelephonyManager.SIM_STATE_READY) {
				sim1_state.setText(R.string.msg_sim_state);
				btSuccess.setEnabled(true);
			} else {
				sim1_state.setText(R.string.msg_sim_state_unknown);
			}
			
			if(true) { //zqf FeatureOption.MTK_GEMINI_SUPPORT && count > 1
				state2 = telMgr.getSimState(1);
				Log.e(TAG, "state2 = " + state2);
				if(state2 == TelephonyManager.SIM_STATE_READY) {
					sim2_state.setText(R.string.msg_sim_state);
					btSuccess.setEnabled(true);
				} else {
					sim2_state.setText(R.string.msg_sim_state_unknown);
				}
			}
		}
	}


	@Override
	public void onClick(View v) {
		
	Log.d(TAG,"onClick v.id="+v.getId());
	switch (v.getId()) {
		case R.id.status_success:
			Utils.SetPreferences(this, mSp, R.string.sim_name, "success");
			finish();
			break;
		case R.id.status_fail:
			Utils.SetPreferences(this, mSp, R.string.sim_name, "failed");
			finish();
			break;

		default:
			break;
		}
	}
	
}
