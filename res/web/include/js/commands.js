// start recording
function startRecording(time, path) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/record", true); // true for asynchronous
    req.send(time + "," + path);
}

// stop recording
function stopRecording() {
    var req = new XMLHttpRequest();
    req.open("GET", "/func/record_stop", true); // true for asynchronous
    req.send();
}