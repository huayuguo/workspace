package com.mediatek.factorymode.fingerprint;

import java.util.ArrayList;
import java.util.List;

import com.microarray.fingerprint.FingerprintTest;
import com.microarray.fingerprint.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.factorymode.R;

public class MAFactoryActivity extends Activity implements OnClickListener { 

	private static final int SPI_TEST = 1;
	private static final int CALIBRATE_TEST = 2;
	private static final int DEADPIX_TEST = 3;
	private static final int INTERRUPT_TEST = 4;
	private static final int PRESS_TEST = 5;
	private static final int ENROLL_TEST = 6;
	private static final int MATCH_TEST = 7;
	private static final int[] DEFAULT_TEST_ITEMS = {SPI_TEST, CALIBRATE_TEST, DEADPIX_TEST, 
		INTERRUPT_TEST, PRESS_TEST, ENROLL_TEST, MATCH_TEST};
	
	private static int UNTEST = -100;
	private static int UNFINISHED = -2;
    private static int[] mTestResults = {UNTEST, UNTEST, UNTEST, UNTEST, UNTEST, UNTEST, UNTEST};
	
	private static final int IDLE = 0;
	private static final int START = 1;
	private static final int WORKING = 2;
	private static final int END = 3;
	private static final int ALL_END = 4;

	private static final int CANCEL_BUTTON_NONE = 0;
	private static final int CANCEL_BUTTON_TOP = 1;
	private static final int CANCEL_BUTTON_BOTTOM = 2;

	public static int ENROLL_TIMES;
	public static int MATCH_TIMES;
	public static int TIMEOUT;
	public static Context CONTEXT;

	private List<TestItem> mTestList;
	private Button btnStart;
    private MARoundProgressBar mEnrollProgress;
	private TextView mEnrollMatchPrompt;
	private TextView mPressTip;
	private TextView mCalibrateTip;
	private SharedPreferences mSp = null;

	private boolean ended = false;
	private boolean paused = false;
    private int mIdentifyCount = 0;
    private boolean mHasCalibrate = false;
    
    private Vibrator mVibrator;
    private final int VIRBATER_TIME_ENROLL = 80;
    private final int VIRBATER_TIME_IDENTIFY = 150;
    private final int VIRBATER_TIME_ENROLL_SUCCESS = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ma_test_sensor);
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        
		for (int i = 0; i < mTestResults.length; i++) {
			mTestResults[i] = UNTEST;
		}
		
		if (getResources().getBoolean(R.bool.enable_vibrator))
			mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
        ENROLL_TIMES = getResources().getInteger(R.integer.enroll_times);
        MATCH_TIMES = getResources().getInteger(R.integer.match_times);
        TIMEOUT = getResources().getInteger(R.integer.press_timeout);
		CONTEXT = this;

		this.mSp = getSharedPreferences("FactoryMode", 0);
        
		setViews();
	}

	private void setViews() {
		setTestItems();
		
		mPressTip = (TextView) findViewById(R.id.press_tip);
        mCalibrateTip = (TextView) findViewById(R.id.calibrate_tip);
        mEnrollProgress = (MARoundProgressBar) findViewById(R.id.enroll_progress);
        mEnrollMatchPrompt = (TextView) findViewById(R.id.enroll_prompt);

		if (getResources().getBoolean(R.bool.show_titile)) {
		    TextView version = (TextView) findViewById(R.id.version_name);
		    version.setText(getVersion());
		} else {
		    LinearLayout title = (LinearLayout) findViewById(R.id.version);
			title.setVisibility(View.GONE);
		}
		Button btnExitTop = (Button) findViewById(R.id.cancel_top);
		Button btnExitBottom = (Button) findViewById(R.id.cancel_bottom);
		switch (getResources().getInteger(R.integer.cancel_position)) {
		case CANCEL_BUTTON_NONE:
			btnExitTop.setVisibility(View.GONE);
			btnExitBottom.setVisibility(View.INVISIBLE);
			break;
		case CANCEL_BUTTON_TOP:
			btnExitBottom.setVisibility(View.INVISIBLE);
			btnExitTop.setOnClickListener(this);
			break;
		case CANCEL_BUTTON_BOTTOM:
			btnExitTop.setVisibility(View.GONE);
			btnExitBottom.setOnClickListener(this);
			break;
		}
		
		btnStart = (Button) findViewById(R.id.start);
		btnStart.setText(getString(R.string.ma_test_start));
		btnStart.setOnClickListener(this);
		btnStart.setEnabled(true);
	}
	
	private void setTestItems() {
		RelativeLayout[] defaultItems = {(RelativeLayout) findViewById(R.id.spi_test),
				(RelativeLayout) findViewById(R.id.calibrate_test),
				(RelativeLayout) findViewById(R.id.deadpixel_test),
				(RelativeLayout) findViewById(R.id.interrupt_test),
				(RelativeLayout) findViewById(R.id.press_test),
				(RelativeLayout) findViewById(R.id.enroll_test),
				(RelativeLayout) findViewById(R.id.match_test)};
		
		TextView[] defaultStates = {(TextView) findViewById(R.id.spi_test_state),
				(TextView) findViewById(R.id.calibrate_test_state),
				(TextView) findViewById(R.id.deadpixel_test_state),
				(TextView) findViewById(R.id.interrupt_test_state),
				(TextView) findViewById(R.id.press_test_state),
				(TextView) findViewById(R.id.enroll_test_state),
				(TextView) findViewById(R.id.match_test_state)};

		setFactoryMode();
		String vendor = FingerprintTest.nativeGetVendor();
		setNormalMode();
		
		mTestList = new ArrayList<TestItem>();
		int[] array = null;
		if (vendor.contains("120")) {
			array = getResources().getIntArray(R.array.test_items_120);
		} else {
			array = getResources().getIntArray(R.array.test_items);
		}
		List<Integer> testIds = new ArrayList<Integer>();
		for (int i = 0; i < array.length; i++) {
			testIds.add(array[i]);
		}

		for (int i = 0; i < DEFAULT_TEST_ITEMS.length; i++) {
			int itemId = DEFAULT_TEST_ITEMS[i];
			if (testIds.contains(itemId)) {
				if (defaultStates[i] != null) {
					mTestList.add(new TestItem(itemId, defaultStates[i]));
					if (itemId == CALIBRATE_TEST) mHasCalibrate = true;
				}
			} else if (defaultItems[i] != null) {
				defaultItems[i].setVisibility(View.GONE);
			}
		}
	}
	
	private void resetStates() {
		for (int i = 0; i < mTestList.size(); i++) {
			mTestList.get(i).stateView.setText(getResources().getString(R.string.ma_state));
			mTestList.get(i).stateView.setTextColor(Color.WHITE);
		}
        mPressTip.setText(getString(R.string.ma_press_hint));
        mPressTip.setTextColor(Color.WHITE);
        mCalibrateTip.setText(getString(R.string.ma_calibrate_hint));
        mCalibrateTip.setTextColor(Color.WHITE);
		mEnrollProgress.setProgress(0);
		mEnrollProgress.doBeginEnrollAnimation();
        mEnrollProgress.setVisibility(View.GONE);
        mEnrollMatchPrompt.setText(R.string.ma_enroll_prepare);
        mEnrollMatchPrompt.setVisibility(View.GONE);
	}
	
	private void resetStartButton() {
		btnStart.setEnabled(true);
		btnStart.setText(getString(R.string.ma_test_restart));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.dprint("onResume");
		paused = false;
		boolean continueTest = false;
		for (int i = 0; i < mTestResults.length; i++) {
			if (mTestResults[i] != UNTEST) {
				continueTest = true;
				break;
			}
		}
		if (continueTest) {
			Utils.dprint("continue test");
			confirmStart();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Utils.dprint("onPause");
		paused = true;
		if (!ended) {
			resetStartButton();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			paused = true;
			Utils.sleep(200);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
			confirmStart();
			break;
		case R.id.cancel_top:
		case R.id.cancel_bottom:
			paused = true;
			Utils.sleep(200);
			com.mediatek.factorymode.Utils.SetPreferences(this, this.mSp, R.string.ma_fingerprint, "failed");
			finish();
			break;
		default:
			break;
		}
	}
	
	private void confirmStart() {
		if (!mHasCalibrate || mTestResults[CALIBRATE_TEST - 1] != UNTEST) {
			btnStart.setEnabled(false);
			resetStates();
			new Thread(mTestRunnable).start();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(MAFactoryActivity.this, 
					android.R.style.Theme_DeviceDefault_Dialog_Alert);
			builder.setTitle(getResources().getString(R.string.ma_calibrate_dialog_title));
			builder.setMessage(getResources().getString(R.string.ma_calibrate_dialog_msg));
			builder.setNegativeButton(getResources().getString(R.string.ma_test_ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							btnStart.setEnabled(false);
							resetStates();
							new Thread(mTestRunnable).start();
						}
				});
			builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
 	
	private Runnable mTestRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (this) {
				ended = !setFactoryMode();
				FingerprintTest.nativeRemove();
				boolean start = false;
				if (mTestResults[MATCH_TEST - 1] != FingerprintTest.PASS && 
								mTestResults[MATCH_TEST - 1] != FingerprintTest.FAIL)
					mTestResults[ENROLL_TEST - 1] = UNTEST;
				int i = 0;
				for (; i < mTestList.size();) {
					if (ended || paused) break;
					Message msg = new Message();
					msg.what = mTestList.get(i).id;
					msg.arg1 = IDLE;
					if (!start) {
						start = true;
						msg.arg1 = START;
						Utils.dprint("mTestResults[" + (msg.what - 1) + "] = " + mTestResults[msg.what - 1]);
						if (mTestResults[msg.what - 1] == FingerprintTest.PASS ||
								mTestResults[msg.what - 1] == FingerprintTest.FAIL) {
							msg.arg1 = END;
							msg.arg2 = mTestResults[msg.what - 1];
						}
					} else {
						start = false;
						if (mTestResults[msg.what - 1] == UNTEST || 
								mTestResults[msg.what - 1] == UNFINISHED) {
							switch(msg.what) {
							case SPI_TEST:
								msg = testSpi(msg);
								break;
							case CALIBRATE_TEST:
								msg = testCalibrate(msg);
								break;
							case DEADPIX_TEST:
								msg = testDeadpix(msg);
								break;	
							case INTERRUPT_TEST:
								msg = testInterrupt(msg);
								break;
							case PRESS_TEST:
								msg = testPress(msg);
								FingerprintTest.nativeRemove();
								break;
							case ENROLL_TEST:
								msg = testEnroll(msg);
								break;
							case MATCH_TEST:
								msg = testMatch(msg);
								break;
							}
						} else {
							ended = mTestResults[i] == FingerprintTest.FAIL;
						}
						if (!ended && !paused) i++;
					}
					if (!paused) {
						mHandler.sendMessage(msg);		
						Utils.sleep(50);
					}
				}
				FingerprintTest.nativeRemove();
				setNormalMode();
				for (int j = 0; !paused && j < mTestResults.length; j++) {
					mTestResults[j] = UNTEST;
				}
				if (i == mTestList.size()) {
					Message msg = new Message();
					msg.arg1 = ALL_END;
					mHandler.sendMessage(msg);
				}
			}
		}
	}; 
	
	private boolean setFactoryMode() {
		int set = FingerprintTest.nativeSet2FactoryMode();
		Utils.dprint("set factory=" + set);
		return set == FingerprintTest.PASS;
	}
	
	private void setNormalMode() {
		int set = FingerprintTest.nativeSet2NormalMode();
		Utils.dprint("set normal=" + set);
	}

	private Message testSpi(Message msg) {
		msg.arg1 = END;
		for (int i = 0; i < 10; i++) {
			Utils.dprint("test spi start i=" + i);
			int ret = FingerprintTest.nativeSPICommunicate();
			Utils.dprint("test spi end. ret=" + ret);
			msg.arg2 = ret;
			if (ret == FingerprintTest.PASS) {
				break;
			}
		}
		if (msg.arg2 != FingerprintTest.PASS) {
			msg.arg2 = FingerprintTest.FAIL;
		}
		ended = msg.arg2 == FingerprintTest.FAIL;
		return msg;
	}

	private Message testCalibrate(Message msg) {
		msg.arg1 = END;
		Utils.dprint("test calibrate start");
		int ret = FingerprintTest.nativeCalibrate();
		Utils.dprint("test calibrate end. ret=" + ret);
		msg.arg2 = ret;
		if (msg.arg2 != FingerprintTest.PASS) {
			msg.arg2 = FingerprintTest.FAIL;
		}
		ended = msg.arg2 == FingerprintTest.FAIL;
		return msg;
	}

	private Message testDeadpix(Message msg) {
		msg.arg1 = END;
		Utils.dprint("test deadpix start");
		int ret = FingerprintTest.nativeBadImage();
		Utils.dprint("test deadpix end. ret=" + ret);
		msg.arg2 = ret;
		if (msg.arg2 != FingerprintTest.PASS) {
			msg.arg2 = FingerprintTest.FAIL;
		}
		ended = msg.arg2 == FingerprintTest.FAIL;
		return msg;
	}

	private Message testInterrupt(Message msg) {
		msg.arg1 = END;
		for (int i = 0; i < 10; i++) {
			Utils.dprint("test interrupt start i=" + i);
			int ret = FingerprintTest.nativeInterrupt();
			Utils.dprint("test interrupt end. ret=" + ret);
			msg.arg2 = ret;
			if (ret == FingerprintTest.PASS) {
				break;
			}
		}
		if (msg.arg2 != FingerprintTest.PASS) {
			msg.arg2 = FingerprintTest.FAIL;
		}
		ended = msg.arg2 == FingerprintTest.FAIL;
		return msg;
	}
	
	private Message testPress(Message msg) {
		long originalTime = System.currentTimeMillis();
		//wait finger down
		while (true) {
			if (ended || paused) break;
			Utils.dprint("test press start");
			long energy = FingerprintTest.nativePress();
			Utils.dprint("test press end. ret=" + energy);
			if (energy > FingerprintTest.NO_FINGER) {
				break;
			} else if (System.currentTimeMillis() - originalTime > TIMEOUT) {
				msg.arg1 = END;
				msg.arg2 = FingerprintTest.FAIL;
				ended = true;
				return msg;
			}
		}
		//wait finger up
		while (true) {
			if (ended || paused) break;
			Utils.dprint("test press start");
			int energy = FingerprintTest.nativePress();
			Utils.dprint("test press end. ret=" + energy);
			if (energy == FingerprintTest.NO_FINGER) {
				msg.arg1 = END;
				msg.arg2 = FingerprintTest.PASS;
				break;
			} else if (System.currentTimeMillis() - originalTime > TIMEOUT) {
				msg.arg1 = END;
				msg.arg2 = FingerprintTest.FAIL;
				ended = true;
				return msg;
			}
		}
		return msg;
	}

	private Message testEnroll(Message msg) {
		long energy = 0;
		int ret = 0;
		int percent = 0;
		while (true) {
			if (ended || paused) break;
			Utils.dprint("test enroll-press start");
			energy = FingerprintTest.nativePress();
			Utils.dprint("test enroll-press end. ret=" + energy);
			if (energy > FingerprintTest.NO_FINGER) {
				//increase progress
				percent += (mEnrollProgress.getProgress() == 0 || ret > FingerprintTest.PASS) ? 
						getEnrollSteps(ENROLL_TIMES) : 0;
				percent = percent > 100 ? 100 : percent;
				Utils.dprint("percent=" + percent);
				if (percent > mEnrollProgress.getProgress()) {
	        		Message uimsg = mHandler.obtainMessage();
	        		uimsg.what = msg.what;
	        		uimsg.arg1 = WORKING;
	                uimsg.arg2 = percent;
					mHandler.sendMessage(uimsg);	
	                if (percent >= 100) {
		        		msg.arg1 = END;
						msg.arg2 = FingerprintTest.PASS;
	                }
				}
				//wait finger up
				while (true) {
					if (ended || paused) break;
					Utils.dprint("test enroll-press start");
					energy = FingerprintTest.nativePress();
					Utils.dprint("test enroll-press end. ret=" + energy);
					if (energy == FingerprintTest.NO_FINGER) {
						break;
					}
				}
				if (ended || paused) break;
				//enroll
				Utils.dprint("test enroll start");
				ret = FingerprintTest.nativeEnroll();
				Utils.dprint("test enroll end. ret=" + ret);
				if (percent >= 100) {
                	break;
                }
			}
		}
		return msg;
	}
	
	private Message testMatch(Message msg) {
		int energy = 0;
		int ret = 0;
		mIdentifyCount = 0;
		while (true) {
			if (ended || paused) break;
			Utils.dprint("test match-press start");
			energy = FingerprintTest.nativePress();
			Utils.dprint("test match-press end. ret=" + energy);
			if (energy > FingerprintTest.NO_FINGER) {
				//authenticate
				for (int i = 0; i < 3; i++) {
					Utils.dprint("test authenticate start");
					ret = FingerprintTest.nativeAuthenticate();
					Utils.dprint("test authenticate ret=" + ret);
					if (ret == FingerprintTest.PASS) break;
					if (i < 2) {
						Utils.dprint("test match-press start");
						energy = FingerprintTest.nativePress();
						Utils.dprint("test match-press end. ret=" + energy);
					}
				}
				//show result
				if (ret == FingerprintTest.PASS) {
	        		msg.arg1 = END;
					msg.arg2 = FingerprintTest.PASS;
					break;
				} else {
					mIdentifyCount++;
					if (mIdentifyCount < MATCH_TIMES) {
		        		Message uimsg = mHandler.obtainMessage();
						uimsg.what = msg.what;
		        		uimsg.arg1 = WORKING;
		                uimsg.arg2 = mIdentifyCount;
						mHandler.sendMessage(uimsg);	
	                } else {
		        		msg.arg1 = END;
						msg.arg2 = FingerprintTest.FAIL;
						FingerprintTest.nativePress();
						ended = true;
		                break;
	                }
				}
				//wait finger up
				while (true) {
					if (ended || paused) break;
					Utils.dprint("test match-press start");
					energy = FingerprintTest.nativePress();
					Utils.dprint("test match-press end. ret=" + energy);
					if (energy == FingerprintTest.NO_FINGER) {
						break;
					}
				}
			}
		}
		return msg;
	}
	
	public static int getEnrollSteps(int times) {
		return (100 % times == 0 ? 100 / times : 100 / times + 1);
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			TextView itemState = null;
			for (int i = 0; i < mTestList.size(); i++) {
				if (mTestList.get(i).id == msg.what)
					itemState = mTestList.get(i).stateView;
			}
			switch (msg.arg1) {
				case START:
					if (itemState == null) break;
					itemState.setText(getResources().getString(R.string.ma_test_run));
					itemState.setTextColor(Color.YELLOW);
					mTestResults[msg.what - 1] = UNFINISHED;
					if (msg.what == ENROLL_TEST) {
						mEnrollMatchPrompt.setText(R.string.ma_enroll_prepare);
						mEnrollProgress.setVisibility(View.VISIBLE);
						mEnrollMatchPrompt.setVisibility(View.VISIBLE);
					} else if (msg.what == MATCH_TEST) {
						mEnrollMatchPrompt.setText(R.string.ma_identify_prepare);
					} else if (msg.what == PRESS_TEST) {
						mPressTip.setText(String.format(getResources().getString(R.string.ma_press_tip), 
								"" + TIMEOUT / 1000));
						mPressTip.setTextColor(Color.YELLOW);
					} else if (msg.what == CALIBRATE_TEST) {
						mCalibrateTip.setText(getResources().getString(R.string.ma_calibrate_tip));
						mCalibrateTip.setTextColor(Color.YELLOW);
					}
					break;
				case WORKING:
					if (itemState == null) break;
					if (msg.what == ENROLL_TEST) {
						int progress = (Integer) msg.arg2;
						if (progress >= 0 && progress <= 100) {
							setDialogProgress(progress);
							touchVibrator(VIRBATER_TIME_ENROLL);
						} else if (progress == -1) {
							setDialogProgress(0);
							mEnrollProgress.doBeginEnrollAnimation();
						}
					} else if (msg.what == MATCH_TEST) {
		             	mEnrollProgress.doIdentifyUndoneAnimation();
						touchVibrator(VIRBATER_TIME_IDENTIFY);
		             	int times = (Integer) msg.arg2;
		             	mEnrollMatchPrompt.setText(getString(R.string.ma_identify_fail) + " (" + times + "/" + 
		             			MATCH_TIMES + "), " + getString(R.string.ma_identify_fail_undone));
					}
					break;
				case END:
					if (itemState == null || paused) break;
					mTestResults[msg.what - 1] = msg.arg2;
					switch (msg.arg2) {
						case FingerprintTest.PASS:
							if (msg.what == ENROLL_TEST) {
				   	            touchVibrator(VIRBATER_TIME_ENROLL_SUCCESS);
							} else if (msg.what == MATCH_TEST) {
				                mEnrollProgress.doIdentifySuccessAnimation();
				                touchVibrator(VIRBATER_TIME_IDENTIFY);
				                mEnrollMatchPrompt.setText(R.string.ma_identify_success);
							}
							itemState.setText(getResources().getString(R.string.ma_test_pass));
							itemState.setTextColor(Color.GREEN);
							break;
						case FingerprintTest.FAIL:
							if (msg.what == MATCH_TEST) {
				                mEnrollProgress.doIdentifyFailAnimation();
				                touchVibrator(VIRBATER_TIME_IDENTIFY);
				              	mEnrollMatchPrompt.setText(getString(R.string.ma_identify_fail) + " (" 
				              			+ MATCH_TIMES + "/" + MATCH_TIMES + ")");
							}
							itemState.setText(getResources().getString(R.string.ma_test_fail));
							itemState.setTextColor(Color.RED);
							Utils.sleep(200);
							resetStartButton();
							break;
					}
					break;
	   			case ALL_END:
	   				Utils.sleep(500);
	   				com.mediatek.factorymode.Utils.SetPreferences(MAFactoryActivity.this, mSp, R.string.ma_fingerprint, "success");
	   				finish();
	   				break;
			}
		}
	};
	
	private String getVersion() {
		String version = "";
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			version = getString(R.string.app_name) + " ver." + info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

    private void setDialogProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            mEnrollProgress.setProgress(progress);
        }
    }
    
    private void touchVibrator(int time) {
    	if (mVibrator != null)
    		mVibrator.vibrate(time);
    }
	
    class TestItem {
		int id;
		TextView stateView;
		
    	public TestItem(int id, TextView stateView) {
    		this.id = id;
    		this.stateView = stateView;
		}
    }

}
