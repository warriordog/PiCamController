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

function hideElementID(elementId) {
    hideElement(document.getElementById(elementId));
}

function hideElement(element) {
    element.style.display = "none";
}

function showFlexElementID(elementID) {
    showFlexElement(document.getElementById(elementID));
}

function showFlexElement(element) {
    element.style.display = "flex";
}