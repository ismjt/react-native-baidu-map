package com.zachary.reactnative.baidumap;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.List;

/**
 * Created by zachary on 2/9/2016.
 */
public class GeolocationModule extends BaseModule
        implements BDLocationListener, OnGetGeoCoderResultListener, LifecycleEventListener {

    private LocationClient locationClient;
    private static GeoCoder geoCoder;
    // 控制onReceiveLocation方法中locationClient.stop();是否关闭
    private Boolean stopLocationClient=true;
    // 设置发起定位请求的间隔时间为2000ms
    private int gpsScanSpan=2000;
    // 是否返回详细的百度定位数据
    private boolean simpleDataType=false;
    // 返回的经纬度坐标系类型，默认百度 bd09ll
    private String coorType = "bd09ll";

    public GeolocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }

    public String getName() {
        return "BaiduGeolocationModule";
    }


    private void initLocationClient() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType(coorType); //返回百度经纬度坐标系
        option.setIsNeedAddress(true);
        option.setIsNeedAltitude(true);
        option.setIsNeedLocationDescribe(true);
        option.setOpenGps(true); //设置是否打开gps进行定位
        option.setScanSpan(gpsScanSpan);// 设置发起定位请求的间隔时间为1000ms
        option.disableCache(true);// 禁止启用缓存定位
        locationClient = new LocationClient(context.getApplicationContext());
        locationClient.setLocOption(option);
        Log.i("locationClient", "locationClient");
        locationClient.registerLocationListener(this);
    }
    /**
     *
     * @return
     */
    protected GeoCoder getGeoCoder() {
        if(geoCoder != null) {
            geoCoder.destroy();
        }
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        return geoCoder;
    }

    /**
     *
     * @param sourceLatLng
     * @return
     */
    protected LatLng getBaiduCoorFromGPSCoor(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;

    }

    /**
     * 设置发起定位请求的间隔时间，默认2000ms
     * @param scanSpan 间隔时间
     */
    @ReactMethod
    public void setScanSpan(int scanSpan) {
        this.gpsScanSpan = scanSpan;
    }

    /**
     * 设置是否返回详细的百度定位数据
     * @param simple 开启开关，默认返回简单的定位数据
     */
    @ReactMethod
    public void setSimpleDataType(boolean simple) {
        this.simpleDataType = simple;
    }

    /**
     * 设置返回的经纬度坐标系类型
     * @param type 定位类型
     */
    @ReactMethod
    public void setCoorType(String type) {
        this.coorType = type;
    }

    /**
     * 是否在每次完成定位后关闭GPS
     * @param stopFlag 关闭标识
     */
    @ReactMethod
    public void stopLocationClient(boolean stopFlag) {
        this.stopLocationClient = stopFlag;
    }

    /**
     * 创建并开启GPS定位客户端
     */
    @ReactMethod
    public void getCurrentPosition() {
        if(locationClient == null) {
            initLocationClient();
        }
        Log.i("getCurrentPosition", "getCurrentPosition");
        locationClient.start();
    }
    @ReactMethod
    public void geocode(String city, String addr) {
        getGeoCoder().geocode(new GeoCodeOption()
                .city(city).address(addr));
    }

    @ReactMethod
    public void reverseGeoCode(double lat, double lng) {
        getGeoCoder().reverseGeoCode(new ReverseGeoCodeOption()
                .location(new LatLng(lat, lng)));
    }

    @ReactMethod
    public void reverseGeoCodeGPS(double lat, double lng) {
        getGeoCoder().reverseGeoCode(new ReverseGeoCodeOption()
                .location(getBaiduCoorFromGPSCoor(new LatLng(lat, lng))));
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        WritableMap params = Arguments.createMap();
        params.putDouble("latitude", bdLocation.getLatitude());
        params.putDouble("longitude", bdLocation.getLongitude());
        params.putDouble("direction", bdLocation.getDirection());
        params.putDouble("altitude", bdLocation.getAltitude());
        params.putDouble("radius", bdLocation.getRadius());
        params.putString("address", bdLocation.getAddrStr());
        params.putDouble("speed", bdLocation.getSpeed());
        // GPS_ACCURACY_GOOD = 1; GPS_ACCURACY_MID = 2; GPS_ACCURACY_BAD = 3;
        params.putInt("accuracy", bdLocation.getGpsAccuracyStatus());
        if(simpleDataType){
            params.putString("countryCode", bdLocation.getCountryCode());
            params.putString("country", bdLocation.getCountry());
            params.putString("province", bdLocation.getProvince());
            params.putString("cityCode", bdLocation.getCityCode());
            params.putString("city", bdLocation.getCity());
            params.putString("district", bdLocation.getDistrict());
            params.putString("street", bdLocation.getStreet());
            params.putString("streetNumber", bdLocation.getStreetNumber());
            params.putString("buildingId", bdLocation.getBuildingID());
            params.putString("buildingName", bdLocation.getBuildingName());
        }
        Log.i("onReceiveLocation", "onGetCurrentLocationPosition");
        sendEvent("onGetCurrentLocationPosition", params);
        // locationClient.stop();
        // 默认是不使用实时定位的
        if(stopLocationClient) {
            locationClient.stop();
        }
    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        WritableMap params = Arguments.createMap();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            params.putInt("errcode", -1);
        }
        else {
            params.putDouble("latitude",  result.getLocation().latitude);
            params.putDouble("longitude",  result.getLocation().longitude);
        }
        sendEvent("onGetGeoCodeResult", params);
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        WritableMap params = Arguments.createMap();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            params.putInt("errcode", -1);
        }
        else {
            ReverseGeoCodeResult.AddressComponent addressComponent = result.getAddressDetail();
            params.putString("address", result.getAddress());
            params.putString("province", addressComponent.province);
            params.putString("city", addressComponent.city);
            params.putString("district", addressComponent.district);
            params.putString("street", addressComponent.street);
            params.putString("streetNumber", addressComponent.streetNumber);

            String briefAddress = result.getAddress(); //简要地址信息
            String businessCircle = result.getBusinessCircle(); //位置所属商圈名称
            String sematicDescription = result.getSematicDescription(); //获取描述信息
            List<PoiInfo> poiInfoList = result.getPoiList();
            WritableArray poiInfos = new WritableNativeArray();
            if(poiInfoList != null && poiInfoList.size() > 0) {
                for(int i = 0; i < poiInfoList.size(); i++) {
                    WritableMap poiInfo = new WritableNativeMap();
                    poiInfo.putString("name", poiInfoList.get(i).name);
                    poiInfo.putString("address", poiInfoList.get(i).address);
                    poiInfo.putString("phoneNum", poiInfoList.get(i).phoneNum);
                    if(poiInfoList.get(i).type != null) {
                        poiInfo.putInt("type", poiInfoList.get(i).type.getInt());
                    }
                    poiInfo.putBoolean("hasCaterDetails", poiInfoList.get(i).hasCaterDetails);
                    poiInfos.pushMap(poiInfo);
                }
            }
            params.putString("briefAddress", briefAddress);
            params.putString("businessCircle", businessCircle);
            params.putString("sematicDescription", sematicDescription);
            params.putArray("poiInfos", poiInfos);

        }
        sendEvent("onGetReverseGeoCodeResult", params);
    }

    @Override
    public void onHostResume() {
        // Activity `onResume`
        if (locationClient != null) {
            locationClient.start();
        }
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
        if (locationClient != null) {
            locationClient.stop();
        }
    }

    @Override
    public void onHostDestroy() {
        // Activity `onDestroy`
        if (locationClient != null) {
            locationClient.stop();
        }
    }

}
