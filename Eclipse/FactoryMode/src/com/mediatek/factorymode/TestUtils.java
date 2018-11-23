
package com.mediatek.factorymode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;

// Gionee xiaolin 20120511 add for CR00596984 start
import com.mediatek.factorymode.R;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
//Gionee xiaolin 20120511 add for CR00596984 end

//import com.mediatek.audioprofile.AudioProfileManager;

public class TestUtils {
    public static WakeLock mWakeLock;

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences mSNSharedPreferences;

    private static ArrayList<String> mItems;

    private static ArrayList<String> mItemKeys;

    private static ArrayList<String> mAutoItemKeys;

    private static ArrayList<String> mAutoItems;
    
    private static ArrayList<String> mKeyItems;
    private static Map<Integer, Character> mReadyToWrite = new HashMap<Integer, Character>();
    private static ArrayList<String> mKeyItemKeys;
    public static boolean mIsAutoMode;

    public static Context mAppContext;
    
    public static int WRITE_TO_SN_COUNT = 4;
    private static SharedPreferences.Editor mEditor;
    public static int VOL_MINUS = 0;
    public static int VOL_MINUS_INCALL = 0;
    
    // Gionee xiaolin 20120806 add for CR00664416 start 
    public static boolean isBatteryTestRestart = true;
    // Gionee xiaolin 20120806 add for CR00664416 end 
    
    // Gionee xiaolin 20120802 add for CR00662674 start 
    public static HashMap<String, Integer> autoTestResult = new HashMap<String, Integer>();
    // Gionee xiaolin 20120802 add for CR00662674 end
    
    // Gionee xiaolin 20120830 add for CR00674046 start
    public static int audioRecBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
    
    public static int audioTrackBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
    
    public static AudioRecord aRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            audioRecBufferSize);
    
    public static AudioTrack aTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            audioTrackBufferSize, AudioTrack.MODE_STREAM);
    // Gionee xiaolin 20120830 add for CR00674046 end
    
    // Gionee xiaolin 20120921 add for CR00693542 start
    static boolean sContinue = false;
    
    public static void checkToContinue(Activity act) {
        if (!sContinue) {
            act.finish();
        }
    }
    // Gionee xiaolin 20120921 add for CR00693542 end
    
    public static SharedPreferences.Editor mSNEditor;
    public static void setAppContext(Activity activity) {
        mAppContext = activity.getApplicationContext();
    }
    public static void acquireWakeLock(Activity activity) {
        if (mWakeLock == null || false == mWakeLock.isHeld()) {
            PowerManager powerManager = (PowerManager) (activity.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE));
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Single Test");
        }
        if (false == mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (null != mWakeLock && true == mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        if (null == mSharedPreferences) {
            mSharedPreferences = context.getSharedPreferences("gn_mmi_test",
                    Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
        }
        return mSharedPreferences;
    }

    public static SharedPreferences getSNSharedPreferences(Context context) {
        if (null == mSNSharedPreferences) {
            mSNSharedPreferences = context.getSharedPreferences("gn_mmi_sn",
                    Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
        }
        return mSNSharedPreferences;
    }

    public static SharedPreferences.Editor getSharedPreferencesEdit(Context context) {
        if (null == mEditor) {
            mSharedPreferences = getSharedPreferences(context);
            mEditor = mSharedPreferences.edit();
        }
        return mEditor;
    }
    
    public static SharedPreferences.Editor getSNSharedPreferencesEdit(Context context) {
        if (null == mSNEditor) {
            mSNSharedPreferences = getSNSharedPreferences(context);
            mSNEditor = mSNSharedPreferences.edit();
        }
        return mSNEditor;
    }
    
    public static ArrayList<String> getItemKeys(Context context) {
        if (null == mItemKeys) {
            mItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_keys)));
        }
        return mItemKeys;
    }

    public static ArrayList<String> getItems(Context context) {
        if (null == mItems) {
            mItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_items)));
        }
        return mItems;
    }

    public static ArrayList<String> getAutoItemKeys(Context context) {
        if (null == mAutoItemKeys) {
            mAutoItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys)));
        }
        return mAutoItemKeys;
    }

    public static ArrayList<String> getAutoItems(Context context) {
        if (null == mAutoItems) {
            mAutoItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items)));
        }
        return mAutoItems;
    }

    public static ArrayList<String> getKeyItems(Context context) {
        // Gionee xiaolin 20120511 modify for CR00596984 start
        if (null == mKeyItems) {
            configKeyTestArrays(context);
        }
        // Gionee xiaolin 20120511 modify for CR00596984 end
        return mKeyItems;
    }

    public static ArrayList<String> getKeyItemKeys(Context context) {
        // Gionee xiaolin 20120511 modify for CR00596984 start
        if (null == mKeyItemKeys) {
            configKeyTestArrays(context);
        }
        // Gionee xiaolin 20120511 modify for CR00596984 end
        return mKeyItemKeys;
    }
    
    // Gionee xiaolin 20121023 modify for CR00717365 start
    public static void rightPress(String TAG, Activity activity) {
        activity.finish();
        processButtonPress(TAG,activity,true);
    }

    public static void wrongPress(String TAG, Activity activity) {
        activity.finish();
        processButtonPress(TAG,activity,false);
    }
    
    private static void processButtonPress(String TAG, Activity activity, boolean success) {
        if (true == mIsAutoMode) {
            mAutoItemKeys = getAutoItemKeys(activity);
            int index = mAutoItemKeys.indexOf(TAG);
            int result = success ? 1 : 0;
            if (null == mEditor) {
                getSharedPreferencesEdit(activity);
            }
            if (index == 0) {
                mEditor.clear();
            }
            mEditor.putInt(TestUtils.getAutoItems(activity).get(index), result);
            mEditor.commit();
            // Gionee xiaolin 20120802 add for CR00662674 start
            autoTestResult.put(TestUtils.getAutoItems(activity).get(index), result);
            SharedPreferences sp = getSharedPreferences(activity);
            int i = sp.getInt(TestUtils.getAutoItems(activity).get(index), result);
            if (i != result) {
                Log.d(TAG," processButtonPress : write result failed one time! try again.");
                mEditor.putInt(TestUtils.getAutoItems(activity).get(index), result);
                mEditor.commit();
            }
            // Gionee xiaolin 20120802 add for CR00662674 end
            
            Log.d("mmi_TestUtils", TestUtils.getAutoItems(activity).get(index) + ":" + result);
            if (index < mAutoItemKeys.size() - 1) {
                try {
                    Intent it = new Intent().setClass(
                            activity,
                            Class.forName("gn.com.android.mmitest.item."
                                    + mAutoItemKeys.get(index + 1)));
                    activity.startActivity(it);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } 
		//else {
              //  Intent it = new Intent(activity, TestResult.class);
              //  it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
              //  activity.startActivity(it);
           // }
        }     
    }
    // Gionee xiaolin 20121023 modify for CR00717365 end
    
    public static void restart(Activity activity, String TAG) {
        try {
            activity.finish();
            Intent it = new Intent(activity, Class
                    .forName("gn.com.android.mmitest.item." + TAG));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(it);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

   
    
    public static String getNewSN(int position, Character value, String sn) {
        StringBuffer sb = new StringBuffer(sn);
        int length = sn.length();
        if (length < position - 1) {
            for (int i = 0; i < position - sn.length(); i++) {
                sb.append(" ");
            }
            sb.append(value);
        } else if (length == position - 1) {
            sb.append(value);
        } else {
            sb.setCharAt(position - 1, value);
        }
        return sb.toString();
    }
    
    public static void openBtAndWifi(Activity activity) {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null != bAdapter && false == bAdapter.isEnabled()) {
            bAdapter.enable();
        }
        
        WifiManager wifiMgr = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (null != wifiMgr && false == wifiMgr.isWifiEnabled()) {
            wifiMgr.setWifiEnabled(true);
        }
    }
    

    private static ArrayList<String> mSingleItemKeys;
    private static ArrayList<String> mSingleItems;
    
    public static void configKeyTestArrays(Context context) {
        mKeyItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_items)));
        mKeyItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_keys)));
        Map<String, String> valueKeyMap = new HashMap<String, String>();
        Map<String, String> propDefMap = new HashMap<String, String>();
        Map<String, Integer> propToResMap = new HashMap<String, Integer>();
        
        if(mKeyItems.size() == mKeyItemKeys.size()) {
            int size = mKeyItemKeys.size();
            for(int i =0; i < size; i++ ) {
                valueKeyMap.put(mKeyItems.get(i), mKeyItemKeys.get(i));
            }
        } else {
            Log.d("TestUtils", "wrong!");
            return;
        }
        
        propDefMap.put("gn.mmi.keytest.menu", "yes");
        propDefMap.put("gn.mmi.keytest.app", "no");
        propDefMap.put("gn.mmi.keytest.search", "no");
        propDefMap.put("gn.mmi.keytest.camera", "no");
        propDefMap.put("gn.mmi.keytest.hall", "no"); 
        
        propToResMap.put("gn.mmi.keytest.menu", R.string.menu_key);
        propToResMap.put("gn.mmi.keytest.app", R.string.app_key);
        propToResMap.put("gn.mmi.keytest.search", R.string.search_key);
        propToResMap.put("gn.mmi.keytest.camera", R.string.camera_key);
        
        
        for ( String prop : propDefMap.keySet()) {
            if (!"yes".equals(SystemProperties.get(prop, propDefMap.get(prop)))) {
                if ("gn.mmi.keytest.hall".equals(prop)) {
                  String hall_o_value = context.getResources().getString(R.string.hall_o_key);
                  String hall_c_value = context.getResources().getString(R.string.hall_c_key);
                  removeKeyItem(hall_o_value, valueKeyMap);
                  removeKeyItem(hall_c_value, valueKeyMap);
                } else {
                  String value = context.getResources().getString(propToResMap.get(prop));
                  removeKeyItem(value, valueKeyMap);
                }
            }
        }
    }
    
    private static void removeKeyItem(String value, Map<String, String> valueToKey) {
        mKeyItems.remove(value);
        mKeyItemKeys.remove(valueToKey.get(value));
    }  
    
    // Gionee xiaolin 20120528 modify for CR00611372 start
    public static void configTestItemArrays(Context context) {
        
        mAutoItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_keys)));
        mAutoItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_items)));
        mSingleItemKeys = new ArrayList<String>(Arrays.asList(context.getResources()
                .getStringArray(R.array.single_test_keys)));
        mSingleItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.single_test_items)));
        
        Map<String, String> featureItem = new HashMap<String, String>();
        String[] featuresToCheck = new String[] {
            PackageManager.FEATURE_CAMERA_FRONT,
            PackageManager.FEATURE_SENSOR_ACCELEROMETER,
            PackageManager.FEATURE_SENSOR_COMPASS,
            PackageManager.FEATURE_SENSOR_LIGHT,
            PackageManager.FEATURE_SENSOR_GYROSCOPE
        };
        
        featureItem.put(PackageManager.FEATURE_CAMERA_FRONT,
                context.getResources().getString(R.string.front_camera));
        featureItem.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                context.getResources().getString(R.string.acceleration));
        featureItem.put(PackageManager.FEATURE_SENSOR_COMPASS, 
                context.getResources().getString(R.string.magnetic_field));
        featureItem.put(PackageManager.FEATURE_SENSOR_LIGHT,
                context.getResources().getString(R.string.light_proximity));
        featureItem.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, 
                context.getResources().getString(R.string.gyroscope));

        PackageManager pm = context.getPackageManager();
        for (String feature : featuresToCheck) {
            if (!hasHardWareFeature(feature)) {
                removeTestItem(featureItem.get(feature));
            }
        }     
        
        if (!"yes".equals(SystemProperties.get("gn.mmi.mic2", "yes"))) {
            String item  = context.getResources().getString(R.string.phone_loopback2);
            removeTestItem(item);
        }
        
        if (!"yes".equals(SystemProperties.get("gn.mmi.receiver2", "no"))) {
            String item  = context.getResources().getString(R.string.receiver2);
            removeTestItem(item);
        }
    }
    
    private static boolean hasHardWareFeature(String feature) {
        
        Map<String, String> featureToSysProp = new HashMap<String, String>();
        featureToSysProp.put( PackageManager.FEATURE_CAMERA_FRONT, "gn.mmi.camera.front");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER, "gn.mmi.sensor.acc");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_COMPASS, "gn.mmi.sensor.compass");
        featureToSysProp.put( PackageManager.FEATURE_SENSOR_LIGHT, "gn.mmi.sensor.light");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_PROXIMITY, "gn.mmi.sensor.prox");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, "gn.mmi.sensor.gyro");
        if (PackageManager.FEATURE_SENSOR_GYROSCOPE.equals(feature)) {
            return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "no"));
        }
        return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
    }
    //  Gionee xiaolin 20120528 modify for CR00611372 end
    
    static private void removeTestItem(String item) {
        int index = mAutoItems.indexOf(item);
        if (-1 != index) {
            mAutoItems.remove(index);
            mAutoItemKeys.remove(index);
        }   
        
        index = mSingleItems.indexOf(item);
        if (-1 != index) {
            mSingleItems.remove(index);
            mSingleItemKeys.remove(index);
        }       
    }
    
    public static String[] getSingleTestItems(Context context) {
        if (null == mSingleItems)
            return null;
        return mSingleItems.toArray(new String[0]);
    }
    
    public static String[] getSingleTestKeys(Context context) {
        if (null == mSingleItemKeys)
            return null;
        return  mSingleItemKeys.toArray(new String[0]);        
    }
    
    // Gionee xiaolin 20121017 add for CR00715318 start
    private static String dAudioState = null;
    public static void muteAudio(Context cxt, boolean mute) {
       // AudioProfileManager apm = (AudioProfileManager)cxt.getSystemService(Context.AUDIOPROFILE_SERVICE);
       // if (null == dAudioState){
        //    dAudioState = apm.getActiveProfileKey();
        //}
        //if(mute)
        //    apm.setActiveProfile("mtk_audioprofile_silent");
       // else
        //    apm.setActiveProfile(dAudioState);
    }
    // Gionee xiaolin 20121017 add for CR00715318 end
}

