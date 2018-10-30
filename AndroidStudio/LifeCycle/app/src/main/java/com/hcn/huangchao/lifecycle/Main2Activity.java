package com.hcn.huangchao.lifecycle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.d(TAG,"Second Activity onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"Second Activity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"Second Activity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"Second Activity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"Second Activity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Second Activity onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"Second Activity onRestart");
    }
}
