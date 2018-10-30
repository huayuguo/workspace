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
import android.widget.EditText;
import android.content.Context;

import com.hcn.huangchao.testdim2.R;

public class MainActivity extends AppCompatActivity {

    private void setBrightnessMode(Context context,int mode) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
                } else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            } else {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mButton = (Button) findViewById(R.id.button);
        final EditText mEdit = (EditText) findViewById(R.id.editView);

        mButton.setOnClickListener(new View.OnClickListener() {
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

                int second = Integer.parseInt(mEdit.getText().toString());
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.System.canWrite(MainActivity.this)) {
                            if(second != 0) {
                                Settings.System.putInt(getContentResolver(), "lightdim_onoff", 1);
                                Settings.System.putInt(getContentResolver(), "lightdim_setting", second * 1000);
                            } else {
                                Settings.System.putInt(getContentResolver(), "lightdim_onoff", 0);
                            }
                        } else {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainActivity.this.startActivity(intent);
                        }
                    } else {
                        if(second != 0) {
                            Settings.System.putInt(getContentResolver(), "lightdim_onoff", 1);
                            Settings.System.putInt(getContentResolver(), "lightdim_setting", second * 1000);
                        } else {
                            Settings.System.putInt(getContentResolver(), "lightdim_onoff", 0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //setBrightnessMode(MainActivity.this, 0);
            }
        });

    }
}
