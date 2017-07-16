// start recording
function sendStartRecording(time, path) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/record/video", true); // true for asynchronous

    req.send(time + "|" + path);
}

// stop recording
function sendStopRecording() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/record/stop", true); // true for asynchronous
    req.send();
}

// take a snapshot
function sendTakeSnapshot(name) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                previewLastSnap();
            }
        }
    };
    req.open("POST", "/func/record/snapshot", true); // true for asynchronous
    req.send(name);
}

var snapTries = 0;

function previewLastSnap() {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                showPreview('p', req.responseText);

                snapTries = 0;
            // not ready yet
            } else if (req.status === 202) {
                // try for 1 minute
                if (snapTries < 60) {
                    window.setTimeout(previewLastSnap(), 1000);
                    snapTries++;
                } else {
                    console.log("Gave up trying to retrieve preview image");
                    snapTries = 0;
                }
            }
        }
    };
    req.open("GET", "/func/media/lastsnap", true); // true for asynchronous
    req.send();
}

// exit program
function sendExitProgram() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/exit", true); // true for asynchronous
    req.send();
}

// reboot pi
function sendRebootSystem() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/reboot", true); // true for asynchronous
    req.send();
}

// resets settings and repopulates field
function sendResetSystemSettings(field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                getSystemSettings(field);
            }
        }
    };
    req.open("GET", "/func/settings/system/reset", true); // true for asynchronous
    req.send();
}

// applies PiCam config
function sendApplySystemSettings(text) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                var req2 = new XMLHttpRequest();
                req2.open("GET", "/func/settings/system/apply", true); // true for asynchronous
                req2.send();
            }
        }
    };
    req.open("POST", "/func/settings/system/set", true); // true for asynchronous
    req.send(text);
}


// saves PiCam config
function sendSaveSystemSettings(text) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/settings/system/save", true); // true for asynchronous
    req.send(text);
}

// populates settings into a field
function getSystemSettings(field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                field.value = req.responseText;
            }
        }
    };
    req.open("GET", "/func/settings/system/get", true); // true for asynchronous
    req.send();
}

// sets camera settings
function sendSaveCamSettings(type, settings) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/settings/camera/set", true); // true for asynchronous
    req.send(type + "&" + settings);
}

// gets settings and fills in field
function populateCamSettings(type, field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                field.value = req.responseText.replace(/\|/g, " ");
            }
        }
    };
    req.open("GET", "/func/settings/camera/get?" + type, true); // true for asynchronous
    req.send();
}

// reset settings and fill in field
function resetCamSettings(type, field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                populateCamSettings(type, field);
            }
        }
    };
    req.open("GET", "/func/settings/camera/reset?" + type, true); // true for asynchronous
    req.send();
}