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
function sendTakeSnapshot(name, div, img) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                loadSnap(div, img);
            }
        }
    };
    req.open("POST", "/func/snapshot", true); // true for asynchronous
    req.send(name);
}

var snapTries = 0;

function loadSnap(div, img) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                div.style.display = "flex";
                img.src = "/func/lastsnap?" + new Date().getTime();

                snapTries = 0;
            } else if (req.status === 202) {
                // not ready yet
                if (snapTries < 40) {
                    window.setTimeout(loadSnap(div, img), 1000);
                    snapTries++;
                } else {
                    console.log("Gave up trying to retrieve preview image");
                    snapTries = 0;
                }
            }
        }
    };
    req.open("GET", "/func/check_snap", true); // true for asynchronous
    req.send(name);
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