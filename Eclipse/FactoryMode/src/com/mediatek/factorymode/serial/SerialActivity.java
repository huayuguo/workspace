package com.mediatek.factorymode.serial;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.yjzn.SerialPort;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;


public class SerialActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "SerialActivity";
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private TextView tvSerialPort, tvSerialBudrate, tvReceiveData;
    private Button btnSend;
    private EditText edtSendMsg;
    public static final String SERIALPORT = "/dev/ttyMT2";
    public static final int SERIALBURATE = 115200;
    byte[] mBuffer;
    private SendingThread mSendingThread;
    private String result = "";
    private Button mBtFailed;
    private Button mBtOk;
    private SharedPreferences mSp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_serial);
        initData();
        initView();
        new ReadThread().start();
        SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
        this.mSp = localSharedPreferences;this.mBtOk = (Button)findViewById(R.id.bt_ok);
        this.mBtOk.setOnClickListener(this);
        this.mBtFailed = (Button)findViewById(R.id.bt_failed);
        this.mBtFailed.setOnClickListener(this);
    }

    private void initView() {
        tvSerialPort = (TextView) findViewById(R.id.tv_serialPort);
        tvSerialBudrate = (TextView) findViewById(R.id.tv_serialBaudrate);
        tvReceiveData = (TextView) findViewById(R.id.tv_receiveData);
        btnSend = (Button) findViewById(R.id.btn_send);
        tvSerialPort.setText(SERIALPORT);
        tvSerialBudrate.setText(SERIALBURATE + "");
        edtSendMsg = (EditText) findViewById(R.id.edt_sendMsg);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtSendMsg.getText().toString().trim())) {
                    Toast.makeText(SerialActivity.this, R.string.sendMsgEmpty, Toast.LENGTH_LONG).show();
                }
                if (mSerialPort != null) {
                    mSendingThread = new SendingThread();
                    mSendingThread.start();
                }
            }
        });
    }


    void initData() {

        try {
            mSerialPort = new SerialPort(new File(SERIALPORT), SERIALBURATE, 0);
            if (mSerialPort == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SerialActivity.this);
                builder.setMessage(getResources().getString(R.string.create_serial_port_fail));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return;
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(SerialActivity.this);
            builder.setMessage(getResources().getString(R.string.create_serial_port_fail));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendingThread extends Thread {
        @Override
        public void run() {
            send(edtSendMsg.getText().toString().trim());
        }
    }

    public void send(final String string) {
        try {
            String s = string;
            s = s.replace(" ", "");
            byte[] bytes = SerialDataUtils.HexToByteArr(s);
            Log.e("Keven","bytes =="+bytes);
            mOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (mInputStream != null) {
                    byte[] buffer = new byte[512];
                    int size = 0;
                    try {
                        size = mInputStream.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (size > 0) {
                        byte[] buffer2 = new byte[size];
                        for (int i = 0; i < size; i++) {
                            buffer2[i] = buffer[i];
                        }
                        result = SerialDataUtils.ByteArrToHex(buffer2).trim();
                        Log.e("Keven","result =="+result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result != null){
                                    Date currentTime = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String dateString = formatter.format(currentTime);
                                    tvReceiveData.append(dateString+" : "+result+"\r\n");
                                }
                            }
                        });
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        mSerialPort =null;
        try {
            mInputStream.close();
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void onClick(View paramView)
    {
      SharedPreferences localSharedPreferences = this.mSp;
      if(paramView.getId() == this.mBtOk.getId()){
          Utils.SetPreferences(this, localSharedPreferences, R.string.serial_test, "success");
          finish();
      }
      else{
          Utils.SetPreferences(this, localSharedPreferences, R.string.serial_test, "failed");
          finish();
      }
    }
}
