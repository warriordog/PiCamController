// start recording
function sendStartRecording(time, path, settings) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/record", true); // true for asynchronous

    req.send(time + "|" + path + "|" + settings);
}

// stop recording
function sendStopRecording() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/record_stop", true); // true for asynchronous
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
    req.open("POST", "/func/snapshot", true); // true for asynchronous
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
    req.open("GET", "/func/lastsnap", true); // true for asynchronous
    req.send();
}

// exit program
function sendExitProgram() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/exit", true); // true for asynchronous
    req.send();
}

// resets settings and repopulates field
function sendResetPiCamConfig(field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                getSettings(field);
            }
        }
    };
    req.open("GET", "/func/reset_settings", true); // true for asynchronous
    req.send();
}

// applies PiCam config
function sendApplyPiCamConfig(text) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                var req2 = new XMLHttpRequest();
                req2.open("GET", "/func/netapply", true); // true for asynchronous
                req2.send();
            }
        }
    };
    req.open("POST", "/func/set_settings", true); // true for asynchronous
    req.send(text);
}


// saves PiCam config
function sendSavePiCamConfig(text) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/save_settings", true); // true for asynchronous
    req.send(text);
}

// reboot pi
function sendRebootSystem() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/reboot", true); // true for asynchronous
    req.send();
}

// gets settings and fills in field
function populateSettings(type, field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                field.value = req.responseText.replace(/\|/g, " ");
            }
        }
    };
    req.open("GET", "/func/getsettings?" + type, true); // true for asynchronous
    req.send();
}

// reset settings and fill in field
function resetSettings(type, field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                populateSettings(type, field);
            }
        }
    };
    req.open("GET", "/func/resetsettings?" + type, true); // true for asynchronous
    req.send();
}

// populates settings into a field
function getSettings(field) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                field.value = req.responseText;
            }
        }
    };
    req.open("GET", "/func/get_settings", true); // true for asynchronous
    req.send();
}