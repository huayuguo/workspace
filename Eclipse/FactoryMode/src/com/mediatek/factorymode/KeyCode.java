package com.mediatek.factorymode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.hardware.camera2.CameraMetadata.Key;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class KeyCode extends Activity implements View.OnClickListener {
//	final int[] imgString;
	Button mBtFailed;
	Button mBtOk;
	private GridView mGrid;
	TextView mInfo;
	MyAdapter mAdapter;
	private List mListData;
	private SharedPreferences mSp;

	ComponentName mCName;
	
	HashMap<Integer, Integer> mKeyToIds;
	/*
	public KeyCode() {
		int[] arrayOfInt = { R.drawable.home, R.drawable.menu,
				R.drawable.vldown, R.drawable.vlup, R.drawable.back,
				R.drawable.search, R.drawable.camera, R.drawable.sos,
				R.drawable.call, R.drawable.unknown, R.drawable.unknown };
		this.imgString = arrayOfInt;
	}*/

	public void onAttachedToWindow() {
		// getWindow().setType(2009);
		super.onAttachedToWindow();
	}

	public void onClick(View paramView) {
		SharedPreferences localSharedPreferences = this.mSp;
		int i = R.string.KeyCode_name;
		if (paramView.getId() == this.mBtOk.getId()) {
			Utils.SetPreferences(this, localSharedPreferences, i, "success");
			finish();
		} else {
			Utils.SetPreferences(this, localSharedPreferences, i, "failed");
			finish();
		}

	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.keycode);
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"FactoryMode", 0);
		this.mSp = localSharedPreferences;
		onAttachedToWindow();
		TextView localTextView = (TextView) findViewById(R.id.keycode_info);
		this.mInfo = localTextView;
		Button localButton1 = (Button) findViewById(R.id.bt_ok);
		this.mBtOk = localButton1;
		this.mBtOk.setOnClickListener(this);
		Button localButton2 = (Button) findViewById(R.id.bt_failed);
		this.mBtFailed = localButton2;
		this.mBtFailed.setOnClickListener(this);
		ArrayList localArrayList = new ArrayList();
		this.mListData = localArrayList;
		GridView localGridView = (GridView) findViewById(R.id.keycode_grid);
		this.mGrid = localGridView;
		mAdapter = new MyAdapter(this);
		mGrid.setAdapter(mAdapter);
		Window window = getWindow();
	//zqf	window.addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);	
		
		mKeyToIds = new HashMap<Integer, Integer>();
		
		mKeyToIds.put(KeyEvent.KEYCODE_HOME, R.drawable.home);
		mKeyToIds.put(KeyEvent.KEYCODE_MENU, R.drawable.menu);
		mKeyToIds.put(KeyEvent.KEYCODE_BACK, R.drawable.back);
		mKeyToIds.put(KeyEvent.KEYCODE_VOLUME_DOWN, R.drawable.vldown);
		mKeyToIds.put(KeyEvent.KEYCODE_VOLUME_UP, R.drawable.vlup);
		mKeyToIds.put(KeyEvent.KEYCODE_SEARCH, R.drawable.search);
		mKeyToIds.put(KeyEvent.KEYCODE_CAMERA, R.drawable.camera);
		mKeyToIds.put(KeyEvent.KEYCODE_CALL, R.drawable.call);
		
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_A, R.drawable.bt_a);
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_B, R.drawable.bt_b);
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_C, R.drawable.bt_c);
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_D, R.drawable.bt_d);
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_E, R.drawable.bt_e);
		mKeyToIds.put(KeyEvent.KEYCODE_YJ_F, R.drawable.bt_f);
		
	}
	

	
	public boolean onKeyDown(int keycode, KeyEvent keyevent) {
		int i = 10;
		int a = 0;
		Integer localInteger = null;
		Log.d("tmp","onKeyDown "+keycode);
		/*
		switch (keycode) {
		case KeyEvent.KEYCODE_CAMERA:
			i = 6;
			break;
		case KeyEvent.KEYCODE_HOME:
			i = 0;
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			i = 2;
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			i = 3;
			break;
		case KeyEvent.KEYCODE_BACK:
			i = 4;
			break;
		case KeyEvent.KEYCODE_MENU:
			i = 1;
			break;
		case KeyEvent.KEYCODE_SEARCH:
			i = 5;
			break;
		case KeyEvent.KEYCODE_SYM:
			i = 7;
			break;
		case KeyEvent.KEYCODE_CALL:
			i = 8;
			break;
		case 0:// ppt
			i = 9;
			break;
		default:
			i = 10;
			break;
		}*/
		if (/*i != 10 &&*/ mAdapter.getCount() < 30) {
			Integer value = mKeyToIds.get(keycode) ;
			
			localInteger = Integer.valueOf(value != null ? value.intValue() : R.drawable.unknown);
			mListData.add(localInteger);
			mAdapter.notifyDataSetChanged();
		}
		return true;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context arg2) {
			// Context localContext;
			LayoutInflater localLayoutInflater = LayoutInflater.from(arg2);
			this.mInflater = localLayoutInflater;
		}

		public MyAdapter(FactoryMode paramInt, int arg3) {
		}

		public int getCount() {
			int count = KeyCode.this.mListData.size();
			/*
			 * Object localObject = KeyCode.this.mListData; if (localObject ==
			 * null); for (localObject = null; ; localObject =
			 * KeyCode.this.mListData.size())
			 */
			return count;
		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			View localView = this.mInflater
					.inflate(R.layout.keycode_grid, null);
			ImageView localImageView = (ImageView) localView
					.findViewById(R.id.imgview);
			int i = ((Integer) KeyCode.this.mListData.get(paramInt)).intValue();
			localImageView.setBackgroundResource(i);
			return localView;
		}
	}



}
