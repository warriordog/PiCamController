// get version
{
    let versionDiv = document.getElementById("controller_version");
    if (versionDiv !== null) {
        camera.getVersionString(function (req, json) {
            versionDiv.innerHTML = json.version;
        });
    }
}

// update status
function updateLoop() {
    let statusDiv = document.getElementById("cam_status");
    let progressDiv = document.getElementById("rec_progress_div");

    camera.getRecordStatus(function (req, json) {
        // record status
        if (json.is_recording) {
            let pathDiv = document.getElementById("recording_path");
            let timeDiv = document.getElementById("recording_time");

            statusDiv.innerHTML = "<div style=\"color: red\">recording</div>";
            pathDiv.innerHTML = json.recording_path;
            timeDiv.innerHTML = (json.recording_time / 1000) + "s";

            showFlexElement(progressDiv);
        } else {
            let recordDiv = document.getElementById("record_row");

            // if progress is visible, then we were recording
            if (isFlexVisible(progressDiv)) {
                hideElement(progressDiv);
                showFlexElement(recordDiv);
            }

            statusDiv.innerHTML = "<div style=\"color: green\">idle</div>";
        }
    }, function (req, json) {
        statusDiv.innerHTML = "<div style=\"color: yellow\">unknown</div>";
    }, function (req, json) {
        statusDiv.innerHTML = "<div style=\"color: grey\">offline</div>";
    });
}

// start update loop
{
    updateLoopTimer = window.setTimeout(updateLoop, 500);
}