package com.hcn.huangchao.testnotifacation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    //private NotificationManager notificationManager = null;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        NotificationChannel channelbody = new NotificationChannel(channel,"消息推送",NotificationManager.IMPORTANCE_DEFAULT);

        on_manmanger.createNotificationChannel(channelbody);

        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.track_writer_service)
                //设置通知标题
                .setContentTitle("最简单的Notification")
                //设置通知内容
                .setContentText("只有小图标、标题、内容");
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(1, builder.build());*/
        String name = "my_package_channel";//渠道名字
        String id = "my_package_channel_1"; // 渠道ID
        String description = "my_package_first_channel"; // 渠道解释说明
        PendingIntent pendingIntent;//非紧急意图，可设置可不设置

        NotificationManager notificationManager = null;
        if (notificationManager == null) {
            notificationManager =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        //判断是否是8.0上设备
        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = null;
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableLights(true); //是否在桌面icon右上角展示小红点
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationBuilder = new NotificationCompat.Builder(this);

            //intent = new Intent(this, DownloadService.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            notificationBuilder.
                    setSmallIcon(R.mipmap.track_writer_service)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.track_writer_service))
                    .setContentTitle("123")
                    .setContentText("正在下载......")
                    //.setContentIntent(pendingIntent)
                    .setChannelId(id)
                    .setAutoCancel(true);
        }else{
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.track_writer_service)
                    .setContentTitle("123")
                    .setContentText("正在下载......")
                    .setAutoCancel(true);

        }


        notificationManager.notify(0, notificationBuilder.build());



    }
}
