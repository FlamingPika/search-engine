var isChrome = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);
if (isChrome) {
	document.getElementById("sidebar").style.display = "block";
}

var input = document.getElementById("search-bar");
var launch = document.getElementById("actual-search");
var form = document.getElementById("the-form");
// Execute a function when the user presses a key on the keyboard
input.addEventListener("keypress", function(event) {
  // If the user presses the "Enter" key on the keyboard
  if (event.keyCode === 13) {
      if (input.value === "" || input.value === null) {
          alert("Please enter a value");
          event.preventDefault();
      } else {
          launch.value = input.value;
          form.submit();
      }
  }
});