package com.hcn.huangchao.remotetest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.util.HashMap;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 */
public class LocationDemo extends Activity implements SensorEventListener {
    private static final String TAG = "LocationDemo";
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    private NetConnectManager mNetConnectManager;
    private NetConnectChangedListener mListener;
    private HashMap<String,LocationDemo.Node> mNode = new HashMap<String,LocationDemo.Node>();
    private int stepDelay;

    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private float direction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        requestLocButton = (Button) findViewById(R.id.button1);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioButtonListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentMarker = null;
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_geo);
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                    mCurrentMode, true, mCurrentMarker,
                                    accuracyCircleFillColor, accuracyCircleStrokeColor));
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        mNetConnectManager = NetConnectManager.getInstance();
        mListener = new MyNetConnectChangedListener();
        mNetConnectManager.registerNetConnectChangedListener(mListener);
        stepDelay = 2000;
        mNetConnectManager.requestServerData(stepDelay);
    }

    class MyNetConnectChangedListener implements NetConnectChangedListener {
        @Override
        public void onLoginStateChange(int state) {
            Log.d(TAG, "onLoginStateChange: " + state);
        }

        @Override
        public void onNodeInfoChange(String param) {
            Log.d(TAG, "onNodeInfoChange: " + param);
            String lines[] = param.split("</br>");
            for (int i = 1 ; i < lines.length ; i++ ) {
                Log.d(TAG, lines[i]);
                UrlParameters.unflatten(lines[i]);
                Log.d(TAG, ">>>>" + UrlParameters.get("longitude") + "," + UrlParameters.get("latitude"));
                wrapNode(UrlParameters.get("mac"),
                        UrlParameters.getDouble("longitude"),
                        UrlParameters.getDouble("latitude"),
                        UrlParameters.getInt("capacity"));
            }
            if(mBaiduMap != null)
                addNodesToMap();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        mNetConnectManager.requestServerData(stepDelay);
    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        mNetConnectManager.removeServerData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    private class Node {
        private String mac;
        private double longitude;
        private double latitude;
        private int capacity;
        public Marker marker = null;

        public Node(String mac, double longitude, double latitude, int capacity) {
            this.mac = mac;
            this.latitude = latitude;
            this.longitude = longitude;
            this.capacity = capacity;
        }

        public void setLatLng(double longitude, double latitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public boolean add() {
            if(longitude == 0 && latitude == 0)
                return false;
            //定义Maker坐标点
            LatLng point = new LatLng(latitude, longitude);
            CoordinateConverter converter  = new CoordinateConverter()
                    .from(CoordinateConverter.CoordType.GPS)
                    .coord(point);
            //desLatLng 转换后的坐标
            LatLng desLatLng = converter.convert();
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_marka);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(desLatLng)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            marker = (Marker) mBaiduMap.addOverlay(option);
            return true;
        }

        public boolean move() {
            if(longitude == 0 && latitude == 0)
                return false;

            //定义Maker坐标点
            LatLng point = new LatLng(latitude, longitude);
            CoordinateConverter converter  = new CoordinateConverter()
                    .from(CoordinateConverter.CoordType.GPS)
                    .coord(point);
            //desLatLng 转换后的坐标
            LatLng desLatLng = converter.convert();
            //通过LatLng列表构造Transformation对象
            Transformation mTransforma = new Transformation(desLatLng);
            //动画执行时间
            mTransforma.setDuration(1000);
            //动画重复模式
            mTransforma.setRepeatMode(Animation.RepeatMode.RESTART);
            //动画重复次数
            mTransforma.setRepeatCount(0);
            //根据开发需要设置动画监听
            mTransforma.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                    Log.d(TAG, "onAnimationStart");
                }

                @Override
                public void onAnimationEnd() {
                    Log.d(TAG, "onAnimationEnd");
                }

                @Override
                public void onAnimationCancel() {
                    Log.d(TAG, "onAnimationCancel");
                }

                @Override
                public void onAnimationRepeat() {
                    Log.d(TAG, "onAnimationRepeat");
                }
            });

            marker.cancelAnimation();
            //设置动画
            marker.setAnimation(mTransforma);
            //开启动画
            marker.startAnimation();

            return true;
        }
    }

    public void addNodesToMap() {
        for (String key : mNode.keySet()) {
            LocationDemo.Node node = mNode.get(key);
            if(node.marker == null) {
                node.add();
            } else {
                node.move();
            }
        }
    }

    private LocationDemo.Node wrapNode(String mac, double longitude, double latitude, int capacity) {
        if (mac == null) return null;
        synchronized (mNode) {
            LocationDemo.Node node = mNode.get(mac);
            if (node == null) {
                node = new  LocationDemo.Node(mac, longitude, latitude, capacity);
            } else  {
                node.setLatLng(longitude, latitude);
                node.setCapacity(capacity);
            }
            mNode.put(mac, node);
            return node;
        }
    }
}
