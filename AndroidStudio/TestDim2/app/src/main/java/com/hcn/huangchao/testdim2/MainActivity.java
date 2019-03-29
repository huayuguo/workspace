package com.hcn.huangchao.testdim2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.content.Context;
import android.widget.Switch;

import com.hcn.huangchao.testdim2.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Timer timer = new Timer();
    EditText wakeEdit;
    EditText mEdit;
    Button delayBtn;
    Button wakeBtn;
    Switch swEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(MainActivity.this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
            }
        }

        delayBtn = (Button) findViewById(R.id.delayBtn);
        wakeBtn = (Button) findViewById(R.id.wakeBtn);
        mEdit = (EditText) findViewById(R.id.editView);
        wakeEdit = (EditText) findViewById(R.id.wakeEdit);
        swEnable = findViewById(R.id.swEnable);

        int s =  Settings.System.getInt(getContentResolver(), "lightdim_setting", 0);
        mEdit.setText("" + s / 1000);
        int c =  Settings.System.getInt(getContentResolver(), "lightdim_onoff", 0);
        swEnable.setChecked(c == 1);

        delayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*int second = Integer.parseInt(mEdit.getText().toString());
                if (Build.VERSION.SDK_INT >= 26) {
                    getWindowManager().setLightDim(second * 1000);
                } else {
                    if(second != 0) {
                        Settings.System.putInt(getContentResolver(), "lightdim_onoff", 1);
                        Settings.System.putInt(getContentResolver(), "lightdim_setting", second * 1000);
                    } else {
                        Settings.System.putInt(getContentResolver(), "lightdim_onoff", 0);
                    }
                }*/

                int second = Integer.parseInt(MainActivity.this.mEdit.getText().toString());
                try {
                    if(second >= 0) {
                        Settings.System.putInt(getContentResolver(), "lightdim_setting", second * 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        wakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int second = Integer.parseInt(wakeEdit.getText().toString());
                MainActivity.this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getWindowManager().setLightDim(-1);
                    }
                },second * 1000);
            }
        });

        swEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if(isChecked) {
                        Settings.System.putInt(getContentResolver(), "lightdim_onoff", 1);
                    } else {
                        Settings.System.putInt(getContentResolver(), "lightdim_onoff", 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
