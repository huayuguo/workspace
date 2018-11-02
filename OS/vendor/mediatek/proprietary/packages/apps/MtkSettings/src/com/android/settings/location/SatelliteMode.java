/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;

//import com.android.internal.logging.MetricsLogger;

import android.provider.Settings;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPreference;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.os.Bundle;

/**
 * A page with 3 radio buttons to choose the location mode.
 *
 * There are 3 location modes when location access is enabled:
 *
 * High accuracy: use both GPS and network location.
 *
 * Battery saving: use network location only to reduce the power consumption.
 *
 * Sensors only: use GPS location only.
 */
public class SatelliteMode extends LocationSettingsBase
	implements RadioButtonPreference.OnClickListener {
    private static final String KEY_GPS_SATELLITE = "gps_satellite";
    private RadioButtonPreference mGps;
    private static final String KEY_BEIDOU_SATELLITE = "beidou_satellite";
    private RadioButtonPreference mBeidou;
    private static final String KEY_GPS_BEIDOU_SATELLITE = "gps_beidou_satellite";
    private RadioButtonPreference mGpsBeidou;
    private static final String KEY_GLONASS_SATELLITE = "glonass_satellite";
    private RadioButtonPreference mGlonass;
    private static final String KEY_GPS_GLONASS_SATELLITE = "gps_glonass_satellite";
    private RadioButtonPreference mGpsGLONASS;
	private int old_mode = -1;
	private int mode = -1;
	private LocationManager mLocationManager = null;

    @Override
    public int getMetricsCategory() {
    	Log.d("SatelliteMode", "getMetricsCategory");
        return MetricsEvent.LOCATION_MODE;
    }

    @Override
    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
        Log.d("SatelliteMode", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
		if(mode != old_mode) {
	        setSatelliteMode(mode);
			Bundle extras = new Bundle();
			extras.putInt("mode", mode);
            mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
                    "set_satellite_mode", extras);
			Log.d("SatelliteMode", "setSatelliteMode mode = "+ mode);
			old_mode = mode;
		}
        Log.d("SatelliteMode", "onPause");
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.satellite_mode);
        root = getPreferenceScreen();

        mGps = (RadioButtonPreference) root.findPreference(KEY_GPS_SATELLITE);
        mBeidou = (RadioButtonPreference) root.findPreference(KEY_BEIDOU_SATELLITE);
        mGpsBeidou = (RadioButtonPreference) root.findPreference(KEY_GPS_BEIDOU_SATELLITE);
        mGlonass = (RadioButtonPreference) root.findPreference(KEY_GLONASS_SATELLITE);
        mGpsGLONASS = (RadioButtonPreference) root.findPreference(KEY_GPS_GLONASS_SATELLITE);
        mGps.setOnClickListener(this);
        mBeidou.setOnClickListener(this);
        mGpsBeidou.setOnClickListener(this);
        mGlonass.setOnClickListener(this);
        mGpsGLONASS.setOnClickListener(this);

        refreshLocationMode();
        
        try {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager == null) {
            	Log.d("SatelliteMode", "new mLocationManager failed");
            }
        } catch (SecurityException e) {
        	Log.d("SatelliteMode", "Exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
        	Log.d("SatelliteMode", "Exception: " + e.getMessage());
        }
        
        return root;
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
        	mGps.setChecked(false);
        	mBeidou.setChecked(false);
        	mGpsBeidou.setChecked(false);
        	mGlonass.setChecked(false);
        	mGpsGLONASS.setChecked(false);
        } else if (activated == mGps) {
        	mGps.setChecked(true);
        	mBeidou.setChecked(false);
        	mGpsBeidou.setChecked(false);
        	mGlonass.setChecked(false);
        	mGpsGLONASS.setChecked(false);
        } else if (activated == mBeidou) {
        	mGps.setChecked(false);
        	mBeidou.setChecked(true);
        	mGpsBeidou.setChecked(false);
        	mGlonass.setChecked(false);
        	mGpsGLONASS.setChecked(false);
        } else if (activated == mGpsBeidou) {
        	mGps.setChecked(false);
        	mBeidou.setChecked(false);
        	mGpsBeidou.setChecked(true);
        	mGlonass.setChecked(false);
        	mGpsGLONASS.setChecked(false);
        }else if (activated == mGlonass) {
        	mGps.setChecked(false);
        	mBeidou.setChecked(false);
        	mGpsBeidou.setChecked(false);
        	mGlonass.setChecked(true);
        	mGpsGLONASS.setChecked(false);
        }else if (activated == mGpsGLONASS) {
        	mGps.setChecked(false);
        	mBeidou.setChecked(false);
        	mGpsBeidou.setChecked(false);
        	mGlonass.setChecked(false);
        	mGpsGLONASS.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == mGps) {
            mode = 1;
			SystemProperties.set("persist.sys.svmode", "1");
            updateRadioButtons(mGps);
        } else if (emiter == mBeidou) {
            mode = 1;
			SystemProperties.set("persist.sys.svmode", "2");
            updateRadioButtons(mBeidou);
        } else if (emiter == mGpsBeidou) {
            mode = 1;
			SystemProperties.set("persist.sys.svmode", "0");
            updateRadioButtons(mGpsBeidou);
        }else if(emiter == mGlonass) {
            mode = 0;
			SystemProperties.set("persist.sys.svmode", "4");
            updateRadioButtons(mGlonass);
        }else if(emiter == mGpsGLONASS) {
            mode = 0;
			SystemProperties.set("persist.sys.svmode", "3");
            updateRadioButtons(mGpsGLONASS);
        }
    }
    
    
    private void setSatelliteMode(int mode){
    	String context = getSatelliteMode();
    	String replacement = "GNSS_MODE="+mode;
    	String s = context.replaceAll("GNSS_MODE=[0-9]", replacement);
    	Log.d("SatelliteMode", "setSatelliteMode mode="+mode+",s="+s);
    	try {
    		File file = new File(Environment.getDataDirectory()+"/misc/gps/mnl.prop");
    		if (!file.exists())
    		{			
    			try {
    				Log.d("SatelliteMode", "setSatelliteMode: create mnl.prop file.");
    				file.createNewFile();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
            FileOutputStream fos = new FileOutputStream(file,false);
            byte [] bytes = s.getBytes();   
            fos.write(bytes);   
            fos.getFD().sync();
            fos.close();
            //Runtime.getRuntime().exec("stop mnld");
            //Runtime.getRuntime().exec("start mnld");
			//SystemProperties.set("persist.sys.mnld", "restart");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	private String getSatelliteMode(){
		String path = Environment.getDataDirectory()+"/misc/gps/mnl.prop";
		String content = "";
		File file = new File(path);
		Log.d("SatelliteMode", "The File exist = " + file.exists());
		if (!file.exists())
		{
			content = "init.speed=115200\nlink.speed=115200\nGNSS_MODE=1\n\n";
			Log.d("SatelliteMode", "content ="+content);
			return content;
		}
		else
		{
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null)
				{
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					while (( line = buffreader.readLine()) != null) {
						content += line + "\n";
					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				e.printStackTrace();
				Log.d("SatelliteMode", "The File doesn't not exist.");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("SatelliteMode", e.getMessage());
			}
		}
		Log.d("SatelliteMode", "content ="+content);
		return content;
	}

    @Override
    public void onModeChanged(int mode, boolean restricted) {
    	Log.d("SatelliteMode", "onModeChanged");
    	if(mode == Settings.Secure.LOCATION_MODE_OFF){
    		updateRadioButtons(null);
    	}else {
			String s = SystemProperties.get("persist.sys.svmode", "0");
			int svmode = 0;
			try {
				svmode = Integer.parseInt(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Log.d("SatelliteMode", "---->>>> svmode="+svmode);
			switch (svmode) {
				case 0:
					updateRadioButtons(mGpsBeidou);
					break;
				case 1:
					updateRadioButtons(mGps);
					break;
				case 2:
					updateRadioButtons(mBeidou);
					break;
				case 4:
					updateRadioButtons(mGlonass);
					break;
				case 3:
					updateRadioButtons(mGpsGLONASS);
					break;
				default:
					updateRadioButtons(null);
					break;
			}
		}

        boolean enabled = (mode != Settings.Secure.LOCATION_MODE_OFF) && !restricted;
        mGpsBeidou.setEnabled(enabled);
        mGps.setEnabled(enabled);
        mBeidou.setEnabled(enabled);
        mGlonass.setEnabled(enabled);
        mGpsGLONASS.setEnabled(enabled);
    }

    @Override
    public int getHelpResource() {
    	Log.d("SatelliteMode", "getHelpResource");
        return R.string.help_url_location_access;
    }
}
