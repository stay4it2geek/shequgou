package com.act.quzhibo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapLongClickListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class GaoDeMapActivity extends Activity implements
        OnMapLongClickListener, AMap.InfoWindowAdapter, OnGeocodeSearchListener, AMap.OnMapLoadedListener {
    AMap aMap;
    MapView mapView;
    GeocodeSearch geocoderSearch;
    View infoWindow;
    LatLng mLatLng;
    UiSettings mUiSettings;
    Marker marker;
    String formatAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_activity);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);
            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
            mUiSettings.setZoomControlsEnabled(true);
            mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
            mUiSettings.setZoomGesturesEnabled(true);
            mUiSettings.setScrollGesturesEnabled(true);
            aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置
        }

        findViewById(R.id.ikown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animOut = AnimationUtils.loadAnimation(GaoDeMapActivity.this, R.anim.anim_marquee_out);
                animOut.setDuration(300);
                findViewById(R.id.ikown).startAnimation(animOut);
                findViewById(R.id.ikown).setVisibility(View.GONE);
            }
        });

    }

    /**
     * amap添加一些事件监听器
     */
    void setUpMap() {
        aMap.setOnMapLoadedListener(this);// 对amap添加地图加载完事件监听器
        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.setInfoWindowAdapter(this);
        aMap.setOnMarkerClickListener(markerClickListener);
        aMap.setOnInfoWindowClickListener(listener);
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                getLatlon(mLatLng);
            }
        });
    }


    AMap.OnInfoWindowClickListener listener = new AMap.OnInfoWindowClickListener() {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Intent intent = new Intent();
            intent.putExtra("lat", marker.getPosition().latitude);
            intent.putExtra("lng", marker.getPosition().longitude);
            intent.putExtra("address", formatAddress + "");
            setResult(RESULT_OK, intent);
            finish();
        }
    };


    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(Marker marker) {
            marker.showInfoWindow();
            return false;
        }
    };

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapLoaded() {
        LatLng latLng = new LatLng(getIntent().getDoubleExtra("lat", 0.0), getIntent().getDoubleExtra("lng", 0.0));
        mLatLng = latLng;
        getLatlon(latLng);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mLatLng = point;
        getLatlon(mLatLng);

    }

    /**
     * 异步查询响应地理坐标
     */
    public void getLatlon(LatLng latLng) {
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocoderSearch.getFromLocationAsyn(query);
    }


    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {

    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        RegeocodeAddress address = result.getRegeocodeAddress();
        formatAddress = address.getFormatAddress() + "附近";
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && address != null && formatAddress != null) {
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        mLatLng, 18, 30, 10)));
                aMap.clear();
                marker = aMap.addMarker(new MarkerOptions().position(mLatLng).snippet(formatAddress)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location))));
                marker.showInfoWindow();
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (infoWindow == null) {
            infoWindow = LayoutInflater.from(this).inflate(
                    R.layout.custom_info_window, null);
        }
        TextView snippet = (TextView) infoWindow.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        return infoWindow;
    }


    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
