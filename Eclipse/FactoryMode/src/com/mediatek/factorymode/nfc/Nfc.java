package com.mediatek.factorymode.nfc;

import com.mediatek.factorymode.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.mediatek.factorymode.Utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.util.Log;

public class Nfc extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  private TextView mInfo;
  private SharedPreferences mSp;

	private NfcAdapter nfcAdapter;
	private TextView resultText;
	private PendingIntent pendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private boolean isFirst = true;
	


  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    if(paramView.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(this, localSharedPreferences, R.string.nfc_name, "success");
        finish();
    }
    else{
        Utils.SetPreferences(this, localSharedPreferences, R.string.nfc_name, "failed");
        finish();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.nfc);
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    TextView localTextView = (TextView)findViewById(R.id.nfc_info);
    this.mInfo = localTextView;
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(this);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			this.mInfo.setText(R.string.no_nfc);
			finish();
			return;
		} else if (!nfcAdapter.isEnabled()) {
			nfcAdapter.enable();
		}
		
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		this.mInfo.setText(R.string.nfc_tips);
    //SDCardSizeTest();
  }
  
  	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			String result = processIntent(intent);
			this.mInfo.setText(result);
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
				mTechLists);
		if (isFirst) {
			if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
				String result = processIntent(getIntent());
				this.mInfo.setText(result);
			}
			isFirst = false;
		}
	}

	protected void onStop() {
		super.onStop();
	}

	protected void onDestroy() {
		super.onDestroy();
	}



	@SuppressLint("NewApi")
	private String processIntent(Intent intent) {
		String resultStr = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
		return resultStr;
	}
	
	
	private String ByteArrayToHexString(byte[] inarray) { // converts byte arrays to string
    int i, j, in;
    String[] hex = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"
    };
    String out = "";

    for (j = 0; j < inarray.length; ++j) {
        in = inarray[j] & 0xff;
        i = (in >> 4) & 0x0f;
        out += hex[i];
        i = in & 0x0f;
        out += hex[i];
    }
    return out;
  }
}