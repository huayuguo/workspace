package com.hcn.huangchao.myhandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button button;
    MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
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
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            String color = b.getString("color");
            //MainActivity.this.button.append(color);
            MainActivity.this.button.setText(color);
        }
    }

    class MyThread implements Runnable {
        public void run() {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Log.d("thread...", "mThread...");
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putString("color", "sleep over" );
            msg.setData(b);

            MainActivity.this.myHandler.sendMessage(msg); // 向Handler发送消息，更新UI
        }
    }
}

