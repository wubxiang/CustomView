package com.example.wbx.baidumaptest;

import android.app.ActionBar;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class MainActivity extends AppCompatActivity {
    private MapView mMapview;
    private BaiduMap mBaiduMap;
    private TextView mAddrTV;
    private TextView mAddrDetailTV;
    private Button mSearchBtn;
    private EditText mCityET;
    private EditText mDetailAddrET;
    private Button mSearchConfirmBtn;
    private Button mLocation;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private double mLatitude;
    private double mLongtitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mMapview = findViewById(R.id.map);
        mBaiduMap = mMapview.getMap();
        mAddrTV = findViewById(R.id.addr);
        mAddrDetailTV = findViewById(R.id.addr2);
        mSearchBtn = findViewById(R.id.search);
        mLocation = findViewById(R.id.location);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopwindow();
            }
        });

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.start();
            }
        });

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setTrafficEnabled(false);//实时路况图
        mBaiduMap.setBaiduHeatMapEnabled(false);//城市热力图


        /**
         * 定位
         */
        mBaiduMap.setMyLocationEnabled(true);
        MyLocationConfiguration cofig = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,null);
        mBaiduMap.setMyLocationConfiguration(cofig);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(mListener);
        //注册监听函数

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，设置定位模式，默认高精度
//LocationMode.Hight_Accuracy：高精度；
//LocationMode. Battery_Saving：低功耗；
//LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
//可选，设置返回经纬度坐标类型，默认GCJ02
//GCJ02：国测局坐标；
//BD09ll：百度经纬度坐标；
//BD09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
//可选，设置发起定位请求的间隔，int类型，单位ms
//如果设置为0，则代表单次定位，即仅定位一次，默认为0
//如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
//可选，设置是否使用gps，默认false
//使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
//可选，定位SDK内部是一个service，并放到了独立进程。
//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
//可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5 * 60 * 1000);
//可选，V7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);
//可选，是否需要地址信息，默认为不需要，即参数为false
//如果开发者需要获得当前点的地址信息，此处必须为true

        option.setIsNeedLocationDescribe(true);
//可选，是否需要位置描述信息，默认为不需要，即参数为false
//如果开发者需要获得当前点的位置信息，此处必须为true


        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

//        mLocationClient.start();
//mLocationClient为第二步初始化过的LocationClient对象
//调用LocationClient的start()方法，便可发起定位请求

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapview.onDestroy();
    }

    BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            mLatitude = location.getLatitude();    //获取纬度信息
            mLongtitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            mAddrTV.setText(addr);

            String locationDescribe = location.getLocationDescribe();    //获取位置描述信息
            mAddrDetailTV.setText(locationDescribe);

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明


            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(mLatitude)
                    .longitude(mLongtitude).build();

            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);

            setZoomLevel();

            mLocationClient.stop();
        }
    };

    private void showPopwindow(){
        View popwindowView = LayoutInflater.from(this).inflate(R.layout.popwindow_layout,null);
        mCityET = popwindowView.findViewById(R.id.city);
        mDetailAddrET = popwindowView.findViewById(R.id.detail_addr);
        mSearchConfirmBtn = popwindowView.findViewById(R.id.search_confirm);

        mSearchConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        PopupWindow popupWindow = new PopupWindow(popwindowView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());//必须设置背景否则点击屏幕popwindow不消失
        popupWindow.showAsDropDown(mSearchBtn);
    }

    private void search(){
        String city = mCityET.getText().toString();
        String detailAddr = mDetailAddrET.getText().toString();

        if(TextUtils.isEmpty(city)){
            return;
        }

//        if(TextUtils.isEmpty(detailAddr)){
//            return;
//        }

       GeoCoder geoCoder =  GeoCoder.newInstance();
       geoCoder.setOnGetGeoCodeResultListener(mGeoCoderResultListener);
        GeoCodeOption geoCodeOption = new GeoCodeOption();
        geoCodeOption.city(city);
        geoCodeOption.address(detailAddr);
       geoCoder.geocode(geoCodeOption);
    }

    OnGetGeoCoderResultListener mGeoCoderResultListener = new OnGetGeoCoderResultListener() {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this,"没有搜索到",Toast.LENGTH_LONG).show();
                return;
            }

            //获取地理编码结果
            LatLng latLng = geoCodeResult.getLocation();
            double latitude = latLng.latitude;
            double longtitude = latLng.longitude;
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longtitude).build();

            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            setZoomLevel();
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
            if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
            }

            //获取地理编码结果
        }
    };

    private void setZoomLevel(){
        MapStatus mapStatus = new MapStatus.Builder().zoom(17).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }
}
