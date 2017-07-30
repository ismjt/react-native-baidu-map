import {
  requireNativeComponent,
  NativeModules,
  Platform,
  DeviceEventEmitter
} from 'react-native';

import React, {
  Component,
  PropTypes
} from 'react';


const _module = NativeModules.BaiduGeolocationModule;

export default {
  // 原生层	stopFlag 默认值为true， 即完成一次定位后关闭客户端
  stopLocationClient(stopFlag) {
	return new Promise((resolve, reject) => {
	  try {
		_module.stopLocationClient(stopFlag);
	  }
	  catch (e) {
		reject(e);
		return;
	  }
	  DeviceEventEmitter.once('onGetCurrentLocationPosition', resp => {
		resolve(resp);
	  });
	});
  },
  // 设置定位请求的时间间隔
  setScanSpan(scanSpan) {
	return new Promise((resolve, reject) => {
	  try {
		_module.setScanSpan(scanSpan);
	  }
	  catch (e) {
		reject(e);
		return;
	  }
	  DeviceEventEmitter.once('onGetCurrentLocationPosition', resp => {
		resolve(resp);
	  });
	});
  },
  // 设置定位数据的返回类型，simple为布尔型参数，原生层默认为false，表示返回简单的百度定位数据
  setSimpleDataType(simple) {
	return new Promise((resolve, reject) => {
	  try {
		_module.setSimpleDataType(simple);
	  }
	  catch (e) {
		reject(e);
		return;
	  }
	  DeviceEventEmitter.once('onGetCurrentLocationPosition', resp => {
		resolve(resp);
	  });
	});
  },
  // 设置返回的经纬度坐标系类型，默认为bd09ll百度坐标，type为枚举型字符串参数，可传入的值有wgs84、gcj02、bd09ll
  setCoorType(type) {
	return new Promise((resolve, reject) => {
	  try {
		_module.setCoorType(type);
	  }
	  catch (e) {
		reject(e);
		return;
	  }
	  DeviceEventEmitter.once('onGetCurrentLocationPosition', resp => {
		resolve(resp);
	  });
	});
  },
  geocode(city, addr) {
    return new Promise((resolve, reject) => {
      try {
        _module.geocode(city, addr);
      }
      catch (e) {
        reject(e);
        return;
      }
      DeviceEventEmitter.once('onGetGeoCodeResult', resp => {
        resolve(resp);
      });
    });
  },
  reverseGeoCode(lat, lng) {
    return new Promise((resolve, reject) => {
      try {
        _module.reverseGeoCode(lat, lng);
      }
      catch (e) {
        reject(e);
        return;
      }
      DeviceEventEmitter.once('onGetReverseGeoCodeResult', resp => {
        resolve(resp);
      });
    });
  },
  reverseGeoCodeGPS(lat, lng) {
    return new Promise((resolve, reject) => {
      try {
        _module.reverseGeoCodeGPS(lat, lng);
      }
      catch (e) {
        reject(e);
        return;
      }
      DeviceEventEmitter.once('onGetReverseGeoCodeResult', resp => {
        resolve(resp);
      });
    });
  },
  getCurrentPosition() {
    if (Platform.OS == 'ios') {
      return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition((position) => {
          _module.getBaiduCoorFromGPSCoor(position.coords.latitude, position.coords.longitude)
            .then((data) => {
              resolve(data);
            })
            .catch((e) => {
              reject(e);
            });
        }, (error) => {
          reject(error);
        }, {
          enableHighAccuracy: true,
          timeout: 20000,
          maximumAge: 1000
        });
      });
    }
    return new Promise((resolve, reject) => {
      try {
        _module.getCurrentPosition();
      }
      catch (e) {
        reject(e);
        return;
      }
      DeviceEventEmitter.once('onGetCurrentLocationPosition', resp => {
        resolve(resp);
      });
    });
  }
};