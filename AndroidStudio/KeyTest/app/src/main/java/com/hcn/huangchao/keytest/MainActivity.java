package com.hcn.huangchao.keytest;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private String TAG = "KeyTest";
    private TextView powerTextView = null;
    private TextView sosTextView = null;
    private TextView volupTextView = null;
    private TextView voldownTextView = null;
    private TextView pptTextView = null;
    private TextView btn1TextView = null;
    private TextView btn2TextView = null;
    private TextView camTextView = null;
    private TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        powerTextView = (TextView)findViewById(R.id.powerTextView);
        sosTextView = (TextView)findViewById(R.id.sosTextView);
        volupTextView = (TextView)findViewById(R.id.volupTextView);
        voldownTextView = (TextView)findViewById(R.id.voldownTextView);
        pptTextView = (TextView)findViewById(R.id.pptTextView);
        btn1TextView = (TextView)findViewById(R.id.btn1TextView);
        btn2TextView = (TextView)findViewById(R.id.btn2TextView);
        camTextView = (TextView)findViewById(R.id.camTextView);
        textView = (TextView)findViewById(R.id.textView);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.v(TAG, "onKeyUp: keyCode = " + keyCode + " event = " + event);
        textView.setText("" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
                powerTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                volupTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                voldownTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case 280:
                sosTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case 281:
                pptTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case 283:
                btn1TextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case 284:
                btn2TextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
            case 282:
                camTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyUp));
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v(TAG, "onKeyDown: keyCode = " + keyCode + " event = " + event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
                powerTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                volupTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                voldownTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case 280:
                sosTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case 281:
                pptTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case 283:
                btn1TextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case 284:
                btn2TextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
            case 282:
                camTextView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorKeyDown));
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
