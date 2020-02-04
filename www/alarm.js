var exec = require('cordova/exec');

var PLUGIN_NAME = 'Alarm';

function onDeviceReady() {
    if (device.platform === "Android") {
        // request read access to the external storage if we don't have it
        cordova.plugins.diagnostic.getExternalStorageAuthorizationStatus(function (status) {
            if (status === cordova.plugins.diagnostic.permissionStatus.GRANTED) {
                console.log("External storage use is authorized");
            } else {
                cordova.plugins.diagnostic.requestExternalStorageAuthorization(function (result) {
                    console.log("Authorization request for external storage use was " + (result === cordova.plugins.diagnostic.permissionStatus.GRANTED ? "granted" : "denied"));
                }, function (error) {
                    console.error(error);
                });
            }
        }, function (error) {
            console.error("The following error occurred: " + error);
        });
    }
}

var Alarm = {
    add: function (successCallback, errorCallback, options) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "add", [options]);
    },
    remove: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "remove", []);
    },
    stop: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "stop", []);
    },
    snooze: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "snooze", []);
    },
    isFromAlarmTrigger: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "isFromAlarmTrigger", []);
    },
    cancelAll: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, PLUGIN_NAME, "cancelAll", []);
    }
};

document.addEventListener("deviceready", onDeviceReady, false);
module.exports = Alarm;

