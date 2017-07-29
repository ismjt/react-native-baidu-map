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