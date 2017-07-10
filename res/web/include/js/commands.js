// start recording
function startRecording(time) {
    var req = new XMLHttpRequest();
    req.open("POST", "/func/record", true); // true for asynchronous
    req.send("time=" + time);
}