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
function takeSnapshot(name) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/snapshot", true); // true for asynchronous
    req.send(name);
}
