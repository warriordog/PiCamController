// start recording
function startRecording(time, path, settings) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/record", true); // true for asynchronous

    req.send(time + "|" + path + "|" + settings);
}

// stop recording
function stopRecording() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/record_stop", true); // true for asynchronous
    req.send();
}

// take a snapshot
function takeSnapshot(name, div, img) {
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
                img.src = "";
                img.src = "/func/lastsnap";

                snapTries = 0;
            } else if (req.status === 202) {
                // not ready yet
                if (snapTries < 20) {
                    window.setTimeout(loadSnap(div, img), 500);
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