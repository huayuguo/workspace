package com.camera.simplewebcam;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Map;

public class PatientInfo extends PreferenceActivity implements OnPreferenceChangeListener {
	public static final String KEY_SERIAL_NUMBER = "number";
	public static final String KEY_NAME = "name";
    //public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";
    public static final String KEY_HOSPITAL_NO = "hospital_no";
    public static final String KEY_PATIENT_NO = "patient_no";
    public static final String KEY_DEPARTMENTS = "departments";
    
    public static final String[] KEYS_PATIENT_INFO = {KEY_SERIAL_NUMBER, KEY_NAME,
    	KEY_AGE, KEY_HOSPITAL_NO, KEY_PATIENT_NO, KEY_DEPARTMENTS};

	protected ActionBar mActionBar;
	private final Map<String, Preference> mKeyToPrefMap = new ArrayMap<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.patient_info);
	    
        for (int i = 0; i < KEYS_PATIENT_INFO.length; i++) {
            final int index = i;
            String preferenceKey = KEYS_PATIENT_INFO[i];

            Preference preference = findPreference(preferenceKey);
            preference.setOnPreferenceChangeListener(this);
            mKeyToPrefMap.put(KEYS_PATIENT_INFO[i], preference);
        }
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        for (int i = 0; i < KEYS_PATIENT_INFO.length; i++) {
            String preferenceKey = KEYS_PATIENT_INFO[i];
            Preference preference = findPreference(preferenceKey);
            if(preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
            	String value = prefs.getString(KEYS_PATIENT_INFO[i], "");
            	if(value.equals("")) {
					value = getResources().getString(R.string.unknow);
				}
                listPreference.setSummary(prefs.getString(KEYS_PATIENT_INFO[i], getResources().getString(R.string.unknow)));
            }
            if(preference instanceof EditTextPreference) {
            	EditTextPreference editPreference = (EditTextPreference) preference;
            	String value = prefs.getString(KEYS_PATIENT_INFO[i], "");
            	if(value.equals("")) {
					value = getResources().getString(R.string.unknow);
				}
            	editPreference.setSummary(value);
            }
        }        
	}

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.e("TAG2", "onPreferenceChange run" + newValue);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = null;
		if(prefs != null){
			editor = prefs.edit();
		}
/*        if(preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            CharSequence[] entries = listPreference.getEntries();
            int index = listPreference.findIndexOfValue((String) newValue);
            listPreference.setSummary(entries[index]);
            Log.d("TAG2", "onPreferenceChange run"+newValue);
            //Toast.makeText(this,entries[index].toString(),Toast.LENGTH_LONG).show();
        }
        if(preference instanceof EditTextPreference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	;
            Log.d("TAG2", "onPreferenceChange run"+newValue);
            //Toast.makeText(ListPreferenceActivity.this,entries[index].toString(),Toast.LENGTH_LONG).show();
        }*/
        if(mKeyToPrefMap.get(KEY_SERIAL_NUMBER) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_SERIAL_NUMBER, (String)newValue);
			editor.commit();
        } else if(mKeyToPrefMap.get(KEY_NAME) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_NAME, (String)newValue);
			editor.commit();
        } else if(mKeyToPrefMap.get(KEY_AGE) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_AGE, (String)newValue);
			editor.commit();
        } else if(mKeyToPrefMap.get(KEY_HOSPITAL_NO) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_HOSPITAL_NO, (String)newValue);
			editor.commit();
        } else if(mKeyToPrefMap.get(KEY_PATIENT_NO) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_PATIENT_NO, (String)newValue);
			editor.commit();
        } else if(mKeyToPrefMap.get(KEY_DEPARTMENTS) == preference) {
        	EditTextPreference editPreference = (EditTextPreference) preference;
        	editPreference.setSummary((CharSequence) newValue);
        	editPreference.setText((String) newValue);
        	editor.putString(KEY_DEPARTMENTS, (String)newValue);
			editor.commit();
        }
        return false;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.patient_menu, menu);
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent();
            intent.setClass(PatientInfo.this, Main.class);
            //intent.putExtra("test", myTextView.getText());
            startActivity(intent);
        	//Toast.makeText(this,"onOptionsItemSelected",Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        return ;
    }
}
