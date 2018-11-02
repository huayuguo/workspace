package com.hcn.huangchao.emgpiotest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static String TAG = "MainActivity";
    private TextView textView;
    MyHandler myHandler;

    private int readAccState()
    {
        int val = 0;
        if (new File("/sys/devices/hall_common@0/gpio_com").exists()) {
            final String filename = "/sys/devices/hall_common@0/gpio_com";
            FileReader reader = null;
            try {
                reader = new FileReader(filename);
                char[] buf = new char[15];
                int n = reader.read(buf);
                if (n > 1) {
                    val = Integer.parseInt(new String(buf, 0, n-1));
                    Log.w(TAG, "readAccState: " + val);
                }
            } catch (IOException ex) {
                Log.w(TAG, "Couldn't read acc state from " + filename + ": " + ex);
            } catch (NumberFormatException ex) {
                Log.w(TAG, "Couldn't read acc state from " + filename + ": " + ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }

        return val;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.gpioSate);

        myHandler = new MyHandler();
        // 当创建一个新的Handler实例时， 它会绑定到当前线程和消息的队列中，开始分发数据
        // Handler有两个作用， (1) : 定时执行Message和Runnalbe 对象
        // (2): 让一个动作，在不同的线程中执行。

        // 它安排消息，用以下方法
        // post(Runnable)
        // postAtTime(Runnable，long)
        // postDelayed(Runnable，long)
        // sendEmptyMessage(int)
        // sendMessage(Message);
        // sendMessageAtTime(Message，long)
        // sendMessageDelayed(Message，long)

        // 以上方法以 post开头的允许你处理Runnable对象
        //sendMessage()允许你处理Message对象(Message里可以包含数据，)

        MyThread m = new MyThread();
        new Thread(m).start();
    }

    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d(TAG, "handleMessage......");
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            String acc_state = b.getString("acc");
            //MainActivity.this.button.append(color);
            MainActivity.this.textView.setText(acc_state);
        }
    }

    class MyThread implements Runnable {
        int acc_state;
        int last_state;
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    acc_state = readAccState();
                    Log.d(TAG, "acc_state = " + acc_state);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (last_state != acc_state) {
                    Log.d(TAG, "mThread...");
                    Message msg = new Message();
                    Bundle b = new Bundle();// 存放数据
                    b.putString("acc", "" + acc_state);
                    msg.setData(b);
                    MainActivity.this.myHandler.sendMessage(msg); // 向Handler发送消息，更新UI
                    last_state = acc_state;
                }
            }
        }
    }
}
