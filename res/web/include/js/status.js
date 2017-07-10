// get version
{
    var versionDiv = document.getElementById("controller_version");
    if (versionDiv !== null) {
        var req = new XMLHttpRequest();
        req.onreadystatechange = function () {
            if (req.readyState === 4) {
                if (req.status === 200) {
                    versionDiv.innerHTML = req.responseText;
                }
            }
        };
        req.open("GET", "/func/version", true); // true for asynchronous
        req.send();
    }
}

// retrieve camera status
function getCameraStatus() {

}

// interval for update loop
var updateLoopInterval = 500;
// timer for update loop
var updateLoopTimer;

// update status
function updateLoop() {
    var statusDiv = document.getElementById("cam_status");

    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var respLine = req.responseText;
                var respArray = respLine.split("|");

                if (respArray.length == 1) {
                    // record status
                    if (respArray[0] === "1") {
                        statusDiv.innerHTML = "<div style=\"color: red\">recording</div>";
                    } else {
                        statusDiv.innerHTML = "<div style=\"color: green\">idle</div>";
                    }
                } else {
                    console.debug("server returned invalid status array: wrong length");
                }
            }

            updateLoopTimer = window.setTimeout(updateLoop, updateLoopInterval);
        }
    };
    req.open("GET", "/func/status", true); // true for asynchronous
    req.send();
}

// start update loop
{
    updateLoopTimer = window.setTimeout(updateLoop, updateLoopInterval);
}