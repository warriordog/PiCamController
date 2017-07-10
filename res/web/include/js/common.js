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