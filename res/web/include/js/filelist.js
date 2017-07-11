// embed a file list
function createFilelist(fileType, divName, categoryName) {
    var div = document.getElementById(divName);
    div.innerHTML = "";

    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                var respArray = req.responseText.split("|");

                var html = "";
                html += "<div class='inner_grid_row'>";
                html += "<div class='grid_item_title'>";
                html += categoryName + " files:";
                html += "</div>";
                html += "<div class='grid_item'>";
                html += "<input type='button' value='Close' onclick=\"hideElementID('" + divName + "')\">";
                html += "</div>";
                html += "</div>";

                for (var i = 0; i < respArray.length; i++) {
                    var entry = respArray[i];
                    var entryParts = entry.split(",");
                    if (entryParts.length === 3) {
                        var fileName = entryParts[0];
                        var fileSize = entryParts[1];
                        var fileTime = entryParts[2];

                        html += "<div class=\"inner_grid_row\">";

                        html += "<div class=\"grid_item\" style='flex-grow: 1'>";
                        html += fileName;
                        html += "</div>";

                        html += "<div class=\"grid_item\">";
                        html += fileSize;
                        html += "</div>";

                        html += "<div class=\"grid_item\">";
                        html += fileTime;
                        html += "</div>";

                        html += "<div class=\"grid_item\">";
                        html += "<a href='/func/download?" + fileType + "=" + fileName + "'>[Download]</a>";
                        html += "</div>";

                        html += "<div class=\"grid_item\">";
                        html += "<a href='#' onclick=\"deleteFile('" + fileType + "', '" + fileName + "', '" + divName + "', '" + categoryName + "')\">[Delete]</a>";
                        html += "</div>";

                        html += "</div>";
                    } else {
                        console.log("Server sent invalid file entry: " + entry)
                    }
                }

                div.innerHTML = html;
            }
        }
    };
    req.open("GET", "/func/listfiles?" + fileType, true); // true for asynchronous
    req.send();
}

// delete a file
function deleteFile(fileType, fileName, divName, categoryName) {
    var req = new XMLHttpRequest();
    var div = document.getElementById(divName);

    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            if (req.status === 200) {
                // if list is still open, then rebuild without deleted file
                if (div.style.display != "none") {
                    createFilelist(fileType, divName, categoryName)
                }
            }
        }
    };
    req.open("POST", "/func/delete", true); // true for asynchronous
    req.send(fileType + "|" + fileName);
}