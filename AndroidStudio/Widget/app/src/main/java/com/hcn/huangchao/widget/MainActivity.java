package com.hcn.huangchao.widget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private RadioGroup genderGroup = null;
    private RadioButton maleButton = null;
    private RadioButton femaleButton = null;
    private CheckBox swimBox = null;
    private CheckBox runBox = null;
    private CheckBox readBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        genderGroup = (RadioGroup)findViewById(R.id.gender);
        maleButton = (RadioButton)findViewById(R.id.male);
        femaleButton = (RadioButton)findViewById(R.id.female);
        swimBox = (CheckBox)findViewById(R.id.swim);
        runBox = (CheckBox)findViewById(R.id.run);
        readBox = (CheckBox)findViewById(R.id.read);

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == maleButton.getId()) {
                    Log.d(TAG, "male");
                } else {
                    Log.d(TAG, "female");
                }
            }
        });

        swimBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Log.d(TAG, "swim checked");
                } else {
                    Log.d(TAG, "swim unchecked");
                }
            }
        });

        runBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Log.d(TAG, "run checked");
                } else {
                    Log.d(TAG, "run unchecked");
                }
            }
        });

        readBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Log.d(TAG, "read checked");
                } else {
                    Log.d(TAG, "read unchecked");
                }
            }
        });
    }
}
