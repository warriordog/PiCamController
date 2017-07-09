// add custom functions
{
    Element.prototype.remove = function() {
        this.parentElement.removeChild(this);
    }
}

// remove javascript warning
{
    var warningMessage = document.getElementById("javascript_warning");
    if (warningMessage !== null) {
        warningMessage.remove();
    }
}

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
