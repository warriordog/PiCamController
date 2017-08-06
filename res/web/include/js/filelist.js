// embed a file list
function createFilelist(isVideo, divName, categoryName) {
    let div = document.getElementById(divName);
    div.innerHTML = "";

    camera.getFileList(isVideo, function (req, json) {
        let html = "";

        if (json.hasOwnProperty("files")) {
            html += "<div class='inner_grid_row'>";
            html += "<div class='grid_item_title'>";
            html += categoryName + " files:";
            html += "</div>";
            html += "<div class='grid_item'>";
            html += "<input type='button' value='Close' onclick=\"hideElementID('" + divName + "')\">";
            html += "</div>";
            html += "</div>";

            for (let i in json.files) {
                let entry = json.files[i];

                html += "<div class=\"inner_grid_row\" style='flex-wrap: nowrap'>";

                html += "<div class=\"grid_item\" style='flex-grow: 1'>";
                html += entry.name;
                html += "</div>";

                html += "<div class=\"grid_item\">";
                html += entry.size;
                html += "</div>";

                html += "<div class=\"grid_item\">";
                html += entry.modified;
                html += "</div>";

                html += "<div class=\"grid_item\">";
                html += "<a href='#' onclick=\"showPreview('" + isVideo + "', '" + entry.name + "')\">[Preview]</a>";
                html += "</div>";

                html += "<div class=\"grid_item\">";
                html += "<a href='/func/media/download?" + isVideo + "=" + entry.name + "'>[Download]</a>";
                html += "</div>";

                html += "<div class=\"grid_item\">";
                html += "<a href='#' onclick=\"deleteFile('" + isVideo + "', '" + entry.name + "', '" + divName + "', '" + categoryName + "')\">[Delete]</a>";
                html += "</div>";

                html += "</div>";

            }
        } else {
            html += "Server error.";
            console.log("Server sent invalid file list");
        }

        div.innerHTML = html;
    });
}

// delete a file
function deleteFile(isVideo, fileName, divName, categoryName) {
    camera.deleteFile(isVideo, fileName, function (req, json) {
        let div = document.getElementById(divName);
        // if list is still open, then rebuild without deleted file
        if (div.style.display !== "none") {
            createFilelist(isVideo, divName, categoryName)
        }
    });
}