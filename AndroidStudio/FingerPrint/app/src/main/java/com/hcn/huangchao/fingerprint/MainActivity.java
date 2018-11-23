package com.hcn.huangchao.fingerprint;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FingerPrintTest";
    private Button check;
    private TextView tvState;
    private FingerprintManagerCompat manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check = findViewById(R.id.btn_check);
        check.setOnClickListener(this);
        tvState = findViewById(R.id.tvState);

        // 获取一个FingerPrintManagerCompat的实例
        manager = FingerprintManagerCompat.from(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                /**
                 * 开始验证，什么时候停止由系统来确定，如果验证成功，那么系统会关系sensor，如果失败，则允许
                 * 多次尝试，如果依旧失败，则会拒绝一段时间，然后关闭sensor，过一段时候之后再重新允许尝试
                 *
                 * 第四个参数为重点，需要传入一个FingerprintManagerCompat.AuthenticationCallback的子类
                 * 并重写一些方法，不同的情况回调不同的函数
                 */
                manager.authenticate(null, 0, null, new MyCallBack(), null);
                tvState.append("开始验证指纹\n");
                break;
        }
    }

    public class MyCallBack extends FingerprintManagerCompat.AuthenticationCallback {
        // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            Log.d(TAG, "onAuthenticationError: " + errString);
            tvState.append("onAuthenticationError: " + errString + "\n");
        }

        // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
        @Override
        public void onAuthenticationFailed() {
            Log.d(TAG, "onAuthenticationFailed: " + "验证失败");
            tvState.append("onAuthenticationFailed: " + "验证失败" + "\n");
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Log.d(TAG, "onAuthenticationHelp: " + helpString);
        }

        // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            Log.d(TAG, "onAuthenticationSucceeded: " + "验证成功");
            tvState.append("onAuthenticationSucceeded: " + "验证成功" + "\n");
        }
    }

}