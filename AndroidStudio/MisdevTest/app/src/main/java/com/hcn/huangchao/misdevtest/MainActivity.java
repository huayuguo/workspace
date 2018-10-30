package com.hcn.huangchao.misdevtest;

import android.hardware.miscdev.MiscDevManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button ircutBtn;
    private Button laserBtn;
    private SeekBar lightBar;
    private Button applyBtn;
    private EditText editText;
    private TextView sensorText;
    private MiscDevManager miscDevMgr;
    private int threshold = 500;
    private int delta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        miscDevMgr = (MiscDevManager) getSystemService(MiscDevManager.MISCDEV_SERVICE);

        ircutBtn = (Button) findViewById(R.id.ircut_btn);
        laserBtn = (Button) findViewById(R.id.laser_btn);
        applyBtn = (Button) findViewById(R.id.applyBtn);;
        editText = (EditText)findViewById(R.id.editText);
        lightBar = (SeekBar) findViewById(R.id.lightBar);

        sensorText = (TextView) findViewById(R.id.sensor_size);

        if(miscDevMgr.getMiscDevState(MiscDevManager.INDEX_IRCUT) == 1) {
            ircutBtn.setText(new String("IR-CUT OFF"));
        } else {
            ircutBtn.setText(new String("IR-CUT ON"));
        }

        if(miscDevMgr.getMiscDevState(MiscDevManager.INDEX_LASER) == 1) {
            laserBtn.setText(new String("Laser OFF"));
        } else {
            laserBtn.setText(new String("Laser ON"));
        }

        ircutBtn.setOnClickListener(this);
        laserBtn.setOnClickListener(this);
        applyBtn.setOnClickListener(this);

        lightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textView.setText("当前进度："+progress+"%");
                threshold = progress;
//                Toast.makeText(MainActivity.this,"" + threshold,Toast.LENGTH_SHORT).show();
                sensorText.setText(""+threshold);
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
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ircut_btn:
                if(ircutBtn.getText().equals(new String("IR-CUT ON"))) {
                    miscDevMgr.setMiscDevState(MiscDevManager.INDEX_IRCUT,MiscDevManager.STATE_ON);
                    ircutBtn.setText(new String("IR-CUT OFF"));
                } else {
                    miscDevMgr.setMiscDevState(MiscDevManager.INDEX_IRCUT,MiscDevManager.STATE_OFF);
                    ircutBtn.setText(new String("IR-CUT ON"));
                }
                break;
            case R.id.laser_btn:
                if(laserBtn.getText().equals(new String("Laser ON"))) {
                    miscDevMgr.setMiscDevState(MiscDevManager.INDEX_LASER,MiscDevManager.STATE_ON);
                    laserBtn.setText(new String("Laser OFF"));
                } else {
                    miscDevMgr.setMiscDevState(MiscDevManager.INDEX_LASER,MiscDevManager.STATE_OFF);
                    laserBtn.setText(new String("Laser ON"));
                }
                break;
            case R.id.applyBtn:
                delta = Integer.parseInt(editText.getText().toString());
                int low = threshold - delta;
                int high = threshold + delta;
                if(low < 0) low = 0;
                miscDevMgr.setLightThreshold(low, high);
                break;
            default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();;
    }
}
