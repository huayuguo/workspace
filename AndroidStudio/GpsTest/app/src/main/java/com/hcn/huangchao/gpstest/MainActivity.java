package com.hcn.huangchao.gpstest;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button startBtn;
    private TextView textLocationShow;
    private TextView mTvMsg;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GPS_LOCATION_NAME = LocationManager.GPS_PROVIDER;

    private LocationManager locationManager;
    private String locateType;
    private String mac;
    private double longitude;
    private double latitude;
    private int capacity;
    private String result;
    private int push_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initViews();
    }

    /**
     * 方法描述：初始化定位相关数据
     */
    private void initData() {
        //获取定位服务
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启GPS定位功能
        locationManager.isProviderEnabled(GPS_LOCATION_NAME);
        //定位类型：GPS
        locateType = locationManager.GPS_PROVIDER;
        //初始化PermissionHelper
        mac = getMacFromHardware();//"02:00:00:00:00:00";
        capacity = getBatteryLevel();
    }

    /**
     * 方法描述：初始化View组件信息及相关点击事件
     */
    private void initViews() {
        textLocationShow = findViewById(R.id.locationTv);
        mTvMsg = findViewById(R.id.httpTv);
        startBtn = findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startBtn.getText() == getResources().getString(R.string.start)) {
                    getLocation();
                    startBtn.setText(R.string.stop);
                } else {
                    locationManager.removeUpdates(locationListener);
                    startBtn.setText(R.string.start);
                }
            }
        });
    }


    private void getLocation() {
        if (MainActivity.this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                MainActivity.this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
            if (location != null) {
                updateUI(location);
                new Thread(postThread).start();
            }
            // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
            locationManager.requestLocationUpdates(locateType, 1000,0,
                    locationListener);
        } else {
            Toast.makeText(this, "checkSelfPermission failed", Toast.LENGTH_LONG).show();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(MainActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
            updateUI(location);
            capacity = getBatteryLevel();
            new Thread(postThread).start();
            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Toast.makeText(MainActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
            getLocation();
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
            updateUI(null);
        }
    };

    /**
     * 方法描述：在View上更新位置信息的显示
     *
     * @param location
     */
    private void updateUI(Location location) {
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            textLocationShow.setText("当前经度：" + longitude + "\n当前纬度：" + latitude);
        } else {
            textLocationShow.setText("Wait Gps data");
        }

    }

    private Thread postThread = new Thread() {
        public void run() {
            String param = new String();
            param = "mac=" + mac + "&longitude=" + longitude + "&latitude=" + latitude + "&capacity=" + capacity;
            Log.e(TAG, "POST: " + param);
            result = HttpURLConnectionClient.getInstance().httpsPost("https://112.74.42.29/a7_demo/update.php", null, param);
            Log.e(TAG, "result: " + result);
            Message msg = Message.obtain();
            msg.what = 0;
            MainActivity.this.postHandler.sendMessage(msg);
        };
    };

    private Handler postHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what==0 && result!=null){
                if(result.startsWith("Success")) {
                    mTvMsg.setText("Upate Ok: " + ++push_count);
                }
            }
        };
    };

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

}
