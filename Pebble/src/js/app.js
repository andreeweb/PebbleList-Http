// Get ready events
Pebble.addEventListener('ready', function() {
  console.log('PebbleKit JS ready.');
  // Update s_js_ready on watch
  Pebble.sendAppMessage({'AppKeyReady': true});
});

// Get AppMessage events
Pebble.addEventListener('appmessage', function(e) {
  // Get the dictionary from the message
  var dict = e.payload;
  console.log('Got message: ' + JSON.stringify(dict));
  makeHTTPRequest(dict['AppKeyUrl']);
});

// My JS Function

/* This function in ready for GET request, but for now we make a simple
  HTTP Request without parameters*/

function makeHTTPRequest(url) {

  var req = new XMLHttpRequest();
  req.open('GET', url, true);
  req.onload = function(e) {
    if (req.readyState == 4) {
      // 200 - HTTP OK
      if(req.status == 200) {
        Pebble.showSimpleNotificationOnPebble("PebbleHttp List", req.responseText);
        console.log('Request returned ' + req.responseText);
      } else {
        Pebble.showSimpleNotificationOnPebble("PebbleHttp List", req.status.toString());
        console.log('Request returned error code ' + req.status.toString());
      }
    }
  };
  req.send(null);
}
