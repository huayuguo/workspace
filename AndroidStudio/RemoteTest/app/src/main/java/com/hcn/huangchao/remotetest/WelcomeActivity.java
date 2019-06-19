package com.hcn.huangchao.remotetest;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    private NetConnectManager mNetConnectManager;
    private Runnable mRunnable;
    private Handler mHandler;
    private NetConnectChangedListener mListener;
    private MyServiceConnecton mConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_welcome);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "login timeout, jump to LoginActivity" );
                Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //WelcomeActivity.this.finish();
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000*10);

        mNetConnectManager = NetConnectManager.getInstance();
        mListener = new NetConnectChangedListener() {
            @Override
            public void onLoginStateChange(int state) {
                Log.d(TAG, "onLoginStateChange: " + state);
                mHandler.removeCallbacks(mRunnable);
                if (state == 1) {
                    Intent intent = new Intent(WelcomeActivity.this, LocationDemo.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                //finish();
            }

            @Override
            public void onNodeInfoChange(String param) {
                Log.d(TAG, "onNodeInfoChange: " + param);;
            }
        };

        mConnect = new MyServiceConnecton() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: " + name);
                mNetConnectManager.registerNetConnectChangedListener(mListener);
                mNetConnectManager.loginServer();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: " + name);
                mNetConnectManager.unRegisterNetConnectChangedListener(mListener);
            }
        };
        mNetConnectManager.setMyServiceConnecton(mConnect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mNetConnectManager.unRegisterNetConnectChangedListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //mNetConnectManager.uninit(this);
    }
}
