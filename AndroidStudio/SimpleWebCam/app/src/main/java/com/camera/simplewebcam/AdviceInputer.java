package com.camera.simplewebcam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toolbar;

public class AdviceInputer extends PreferenceActivity implements OnPreferenceChangeListener {
	private static final String TAG="AdviceInputer";
	//ListPreference attitudePreference;
	//ListPreference projectPreference;
	EditTextPreference advicePreference;
    EditTextPreference doctorPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        //setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

	    addPreferencesFromResource(R.xml.advice_inputer);
	    //attitudePreference = (ListPreference)findPreference("attitude");
	    //attitudePreference.setOnPreferenceChangeListener(this);
	    //projectPreference = (ListPreference)findPreference("project");
	    //projectPreference.setOnPreferenceChangeListener(this);
	    advicePreference = (EditTextPreference)findPreference("advice");
	    advicePreference.setOnPreferenceChangeListener(this);
        doctorPreference = (EditTextPreference)findPreference("doctor");
        doctorPreference.setOnPreferenceChangeListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String doctor = prefs.getString("doctor", "");
        doctorPreference.setSummary(doctor);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v(TAG, "onCreate");
	}

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.e(TAG, "onPreferenceChange run" + newValue);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = null;
		if(prefs != null){
			editor = prefs.edit();
		}
		
        /*if(attitudePreference == preference) {
        	attitudePreference.setSummary((CharSequence) newValue);
        	//attitudePreference.setTitle((String) newValue);
        	editor.putString("attitude", (String)newValue);
			editor.commit();
        } else if(projectPreference == preference) {
        	projectPreference.setSummary((CharSequence) newValue);
        	//projectPreference.setTitle((String) newValue);
        	editor.putString("project", (String)newValue);
			editor.commit();
        } else */
        if(advicePreference == preference) {
        	advicePreference.setSummary((CharSequence) newValue);
        	//advicePreference.setText((String) newValue);
        	editor.putString("advice", (String)newValue);
			editor.commit();
        } else if(doctorPreference == preference) {
            doctorPreference.setSummary((CharSequence) newValue);
            //advicePreference.setText((String) newValue);
            editor.putString("doctor", (String)newValue);
            editor.commit();
        }

        return false;
    }
}

