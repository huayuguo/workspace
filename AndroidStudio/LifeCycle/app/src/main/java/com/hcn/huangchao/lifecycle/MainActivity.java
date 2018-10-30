package com.hcn.huangchao.lifecycle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button myButton = null;

    class MyButtonListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, Main2Activity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"First Activity onCreate");

        myButton = (Button)findViewById(R.id.myButton);
        //myButton.setText(R.string.start_activity);
        myButton.setOnClickListener(new MyButtonListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"First Activity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"First Activity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"First Activity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"First Activity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"First Activity onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"First Activity onRestart");
    }
}
