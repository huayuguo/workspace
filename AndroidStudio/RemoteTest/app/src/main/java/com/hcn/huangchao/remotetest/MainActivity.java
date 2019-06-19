package com.hcn.huangchao.remotetest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient;
    private MyLocationListener mLocatioLnistener;
    private MyLocationConfiguration mLocationConfiguration;
    private NetConnectManager mNetConnectManager;
    private NetConnectChangedListener mListener;
    private HashMap<String,Node> mNode = new HashMap<String,Node>();
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private MyLocationData locData;
    boolean isFirstLoc = true;

    class Node {
        private String mac;
        private double longitude;
        private double latitude;
        private int capacity;
        public Marker marker;

        Node(String mac, double longitude, double latitude, int capacity) {
            this.mac = mac;
            this.latitude = latitude;
            this.longitude = longitude;
            this.capacity = capacity;
        }

        void setLatLng(double longitude, double latitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        void setCapacity(int capacity) {
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
            mTransforma.setDuration(500);
            //动画重复模式
            mTransforma.setRepeatMode(Animation.RepeatMode.RESTART);
            //动画重复次数
            mTransforma.setRepeatCount(1);
            //根据开发需要设置动画监听
            mTransforma.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {

                }
            });

            //设置动画
            marker.setAnimation(mTransforma);
            //开启动画
            marker.startAnimation();

            return true;
        }
    }

    public void addNodesToMap() {
        for (String key : mNode.keySet()) {
            Node node = mNode.get(key);
            if(node.marker == null) {
                node.add();
            } else {
                node.move();
            }
        }
    }

    private Node wrapNode(String mac, double longitude, double latitude, int capacity) {
        if (mac == null) return null;
        synchronized (mNode) {
            Node node = mNode.get(mac);
            if (node == null) {
                node = new Node(mac, longitude, latitude, capacity);
            } else  {
                node.setLatLng(longitude, latitude);
                node.setCapacity(capacity);
            }
            mNode.put(mac, node);
            return node;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //普通地图 ,mBaiduMap是地图控制器对象
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomBy(4);
        mBaiduMap.animateMapStatus(mapStatusUpdate);

        initLocationOption();

        mNetConnectManager = NetConnectManager.getInstance();
        mListener = new MyNetConnectChangedListener();
        mNetConnectManager.registerNetConnectChangedListener(mListener);
        mNetConnectManager.requestServerData(2000);
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
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNetConnectManager.unRegisterNetConnectChangedListener(mListener);
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    /**
     * 初始化定位参数配置
     */

    private void initLocationOption() {
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mLocationClient= new LocationClient(getApplicationContext());
        //声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        mLocatioLnistener = new MyLocationListener();
        //注册监听函数
        mLocationClient.registerLocationListener(mLocatioLnistener);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        //可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(locationOption);
        mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true,null);
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
        mLocationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
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
    }
}
