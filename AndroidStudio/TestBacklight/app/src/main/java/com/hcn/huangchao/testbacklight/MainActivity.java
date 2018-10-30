package com.hcn.huangchao.testbacklight;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SeekBar lightBar;
    private SeekBar sysLightBar;
    private TextView brightnessText;
    private TextView sysBrightnessText;
    private Switch modeSwitch;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lightBar = (SeekBar) findViewById(R.id.seekBar);
        sysLightBar = (SeekBar) findViewById(R.id.seekBar2);
        brightnessText = (TextView) findViewById(R.id.brightnessText);
        sysBrightnessText = (TextView) findViewById(R.id.sysBrightnessText);
        modeSwitch = (Switch) findViewById(R.id.modeSwitch);
        button = (Button) findViewById(R.id.button);

        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        int brightness = getBrightness(this);
        lightBar.setProgress(brightness);
        brightnessText.setText("Activity Brightness:" + brightness);
        lightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
             * seekbar改变时的事件监听处理
             * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textView.setText("当前进度："+progress+"%");
                brightnessText.setText("Activity Brightness:" + progress);
                setBrightness(MainActivity.this, progress);
                Log.d("debug",String.valueOf(seekBar.getId()));
            }
            /*
             * 按住seekbar时的事件监听处理
             * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
            }
            /*
             * 放开seekbar时的时间监听处理
             * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
            }
        });
        sysLightBar.setProgress(brightness);
        sysBrightnessText.setText("System Brightness:" + brightness);
        sysLightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
             * seekbar改变时的事件监听处理
             * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textView.setText("当前进度："+progress+"%");
                sysBrightnessText.setText("System Brightness:" + progress);
                saveBrightness(MainActivity.this, progress);
                Log.d("debug",String.valueOf(seekBar.getId()));
            }
            /*
             * 按住seekbar时的事件监听处理
             * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
            }
            /*
             * 放开seekbar时的时间监听处理
             * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
            }
        });

        if(isAutoBrightness(this))
            modeSwitch.setChecked(true);

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    startAutoBrightness(MainActivity.this);
                } else {
                    stopAutoBrightness(MainActivity.this);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrightness(MainActivity.this, -1);
            }
        });
    }

    public boolean isAutoBrightness(Activity activity) {
        boolean autoBrightness = false;
        ContentResolver contentResolver = activity.getContentResolver();
        try {
            autoBrightness = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return autoBrightness;
    }

    // 获取当前系统亮度值
    public static int getBrightness(Activity activity) {
        int brightValue = 0;
        ContentResolver contentResolver = activity.getContentResolver();
        try {
            brightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }

    // 改变屏幕亮度
    public void setBrightness(Activity activity, int brightValue) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = (brightValue <= 0 ? -1.0f : brightValue / 255f);
        activity.getWindow().setAttributes(lp);
    }

    public void saveBrightness(Activity activity, int brightness) {
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        activity.getContentResolver().notifyChange(uri, null);
    }


    // 开启亮度自动亮度模式
    public void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        activity.getContentResolver().notifyChange(uri, null);
    }

    // 停止自动亮度模式
    public void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        activity.getContentResolver().notifyChange(uri, null);
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    public void setBrightnessMode(Activity activity, int brightMode) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, brightMode);
    }
}
