// Camera class
const camera = {
    // "private" variables
    _defaultSuccessCallback : function(req, json) {},
    _defaultFailureCallback : function(req, json) {},
    _defaultErrorCallback : function(req) {},

    /*
     network functions
    */
    // sends a GET request
    _sendGET : function(uri, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        let req = new XMLHttpRequest();
        req.onreadystatechange = function () {
            if (req.readyState === 4) {
                try {
                    let json = JSON.parse(req.responseText);
                    if (req.status === 200) {
                        //success
                        successCallback(req, json);
                    } else {
                        // server errors
                        failureCallback(req, json);
                    }
                } catch (e) {
                    // json errors
                    errorCallback(req);
                }
            }
        };
        // network errors
        req.onerror = errorCallback(req);
        req.open("GET", uri, true); // true for asynchronous
        req.send();
    },

    // sends a POST request
    _sendPOST : function(uri, json, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        let req = new XMLHttpRequest();
        req.onreadystatechange = function () {
            if (req.readyState === 4) {
                try {
                    let json = JSON.parse(req.responseText);
                    if (req.status === 200) {
                        //success
                        successCallback(req, json);
                    } else {
                        // server errors
                        failureCallback(req, json);
                    }
                } catch (e) {
                    // json errors
                    errorCallback(req);
                }
            }
        };
        // network errors
        req.onerror = errorCallback(req);
        req.open("POST", uri, true); // true for asynchronous
        req.send(JSON.stringify(json));
    },



    /*
        Camera Functions
     */

    // gets the version of PiCam running on the server
    getVersionString : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/admin/version", successCallback, failureCallback, errorCallback);
    },

    // gets the status of the current recording
    getRecordStatus : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        this._sendGET("/func/record/status", successCallback, failureCallback, errorCallback);
    },
    // starts recording a video
    recordVideo : function(time, filename, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        let json = {
            time: time,
            filename: filename
        };
        this._sendPOST("/func/record/video", json, successCallback, failureCallback, errorCallback);
    },
    // records a snapshot
    recordSnapshot : function(filename, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        let json = {
            filename: filename
        };
        this._sendPOST("/func/record/snapshot", json, successCallback, failureCallback, errorCallback);
    },
    // stop recording
    stopRecording : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback) {
        this._sendGET("/func/record/stop", successCallback, failureCallback, errorCallback);
    },
    // get the filename of the last snapshot (for preview)
    getSnapshotPreview : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback, numTries = 0) {
        this._sendGET("/func/media/lastsnap", successCallback, function (req, json) {
            // waiting
            if (req.status === 202) {
                if (numTries < 60) {
                    window.setTimeout(function () {
                        this.getSnapshotPreview(successCallback, failureCallback, errorCallback, numTries + 1)
                    }, 500);
                } else {
                    failureCallback(req, json);
                }
            } else {
                failureCallback(req, json);
            }
        }, errorCallback);
    },

    //exit the PiCam software
    exitProgram : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/admin/exit", successCallback, failureCallback, errorCallback);
    },
    //shutdown camera
    shutdownPi : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/admin/shutdown", successCallback, failureCallback, errorCallback);
    },
    //reboot camera
    rebootPi : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/admin/reboot", successCallback, failureCallback, errorCallback);
    },

    // sync filesystem
    syncFS : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/fs/sync", successCallback, failureCallback, errorCallback);
    },
    // remount filesystem
    remountFS : function(state, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            state: state
        };
        this._sendPOST("/func/fs/mount", json, successCallback, failureCallback, errorCallback);
    },
    // erase video cache
    eraseCache : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/fs/clear_cache", json, successCallback, failureCallback, errorCallback);
    },

    // reset settings
    resetSystemSettings : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/settings/system/reset", json, successCallback, failureCallback, errorCallback);
    },
    // apply system settings
    applySystemSettings : function(settings, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendPOST("/func/settings/system/set", settings, successCallback, failureCallback, errorCallback);
    },
    //save current system settings
    saveSystemSettings : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/settings/system/save", successCallback, failureCallback, errorCallback);
    },
    //get current system settings
    getSystemSettings : function(successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        this._sendGET("/func/settings/system/get", successCallback, failureCallback, errorCallback);
    },

    // reset camera settings
    resetCamSettings : function(isVideo, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            is_video: isVideo
        };
        this._sendPOST("/func/settings/camera/reset", json, successCallback, failureCallback, errorCallback);
    },
    // apply camera settings
    applyCamSettings : function(isVideo, settings, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            is_video: isVideo,
            settings: settings
        };
        this._sendPOST("/func/settings/camera/apply", json, successCallback, failureCallback, errorCallback);
    },
    // get camera settings
    getCamSettings : function(isVideo, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            is_video: isVideo
        };
        this._sendPOST("/func/settings/camera/get", json, successCallback, failureCallback, errorCallback);
    },

    getFileList : function(isVideo, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            is_video: isVideo
        };
        this._sendPOST("/func/media/list", json, successCallback, failureCallback, errorCallback);
    },

    // delete a file
    deleteFile : function(isVideo, fileName, successCallback = this._defaultSuccessCallback, failureCallback = this._defaultFailureCallback, errorCallback = this._defaultErrorCallback){
        let json = {
            is_video: isVideo,
            filename: fileName
        };
        this._sendPOST("/func/media/delete", json, successCallback, failureCallback, errorCallback);
    }
};