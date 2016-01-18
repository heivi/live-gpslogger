/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
var app = {
    // Application Constructor
    initialize: function () {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function () {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function () {
        $(function () {
            //console.log(cordova);
            cordova.plugins.backgroundMode.setDefaults({
                title: "KeparDI GPS running",
            });
            
            document.addEventListener("resume", onResume, false);
            
            run();
        });

    }
};

//var track = new Array();
//var GPX = new Array();
//var tme = 0;
//var gpxname = '';
var playing = 0;
var started = 0;
//var ptimepre = 0;
//var distance = 0;
//var resolution = 1; // seconds between samples.

var points = 0;
var lastok = false;

var buffer = new Array();

var watchid = 0;
var tunnus = "none";
var server = "http://gps.virekunnas.fi";

app.initialize();

/*
function startlogger() {
    ptimepre = 0;
    if (!playing) {
        playing = 1;
        $("#startlogger").html("Stop logging");
        //cordova.plugins.backgroundMode.enable();
    } else {
        playing = 0;
        $("#startlogger").html("Start logging");
        //cordova.plugins.backgroundMode.disable();
    }
}*/

function startgps() {
    //alert("startgps()");
    //$("#location").html("Trying...");
    if (tunnus == "none" || tunnus == "") {
        alert("Choose tracking id first!");
        return false;
    }

    if (!started && tunnus != "none" && tunnus != "") {
        started = 1;
        $("#location").html("Trying...");
        if (navigator.geolocation) {

            watchid = navigator.geolocation.watchPosition(
              foundLocation,
              noLocation, {
                  enableHighAccuracy: true,
                  timeout: 15000,
                  maximumAge: 15000
              });
        } else {
            $("#location").html("Not supported!");
        }
    } else if (!started) {
        alert("Already started!");
    }
    return true;
}

function foundLocation(p) {
    //alert(p);

    var lat = p.coords.latitude;
    var lng = p.coords.longitude;
    var accuracy = p.coords.accuracy;
    var timestamp = p.timestamp;

    timestamp = parseInt(timestamp / 1000);

    if (timestamp < 1445407893) {
        timestamp = parseInt(new Date().getTime() / 1000);
    }

    var posData = {
        lat: lat,
        lon: lng,
        acc: accuracy,
        c: tunnus,
        time: parseInt(timestamp)
    };

    // to be sent
    buffer.push(posData);
    $("#buffer").html(buffer.length);

    // send to server
    /*$.ajax({
        url: server + '/save.php',
        data: posData,
        success: function (data) {
            //$("#console").append("Called: "+data.toString()+"\n");
            if (data != "OK") {
                if (lastok) {
                    $("#location").append("<br>Error sending position...");
                }
                lastok = false;
                buffer.push(posData);
                $("#buffer").html(buffer.length);
            } else {
                lastok = true;
            }
        },
        error: function (xhr, status, error) {
            if (data != "OK") {
                if (lastok) {
                    $("#location").append("<br>Error sending position...");
                }
                lastok = false;
                buffer.push(posData);
                $("#buffer").html(buffer.length);
            }
        }
    });*/

    /*$("#location").html("<nobr>Lat:"+(Math.floor(lat*10000000)/10000000)+
    "<br>Lon:"+(Math.floor(lng*10000000)/10000000) +"<br>UTC Time:" +
    ISODateString(d) + "<br>Accuracy:"+Math.floor(accuracy)+
    "<br>Points saved: "+track.length+"<br>Time: "+
    addzero(Math.floor(tme/60))+":"+addzero(tme-Math.floor(tme/60)*60)+
    "<br>Distance:"+(Math.floor(distance*100)/100) +" km</nobr>");*/

    $("#location").html("Tracking on! Points found: " + (points++) + "<br>Accuracy: " + accuracy + " m");

    /*if (playing && accuracy < 40 && accuracy > 0.01 && ptime - resolution > ptimepre - 1) {
        if (ptimepre > 0) {
            tme = tme + ptime - ptimepre;
            distance = 1 * distance + 1 * calculateDistance(lat, lng, prelat, prelng);
        }
        if (gpxname == '') {
            gpxname = 'log_' + ISODateString(d);
        }
        track.push('' + ptime + ',' + ISODateString(d) + ',' +
        Math.floor(lat * 10000000) / 10000000 + ',' +
        Math.floor(lng * 10000000) / 10000000 + "," +
        Math.floor(distance * 1000));

        GPX.push('<trkpt lat="' + Math.floor(lat * 10000000) / 10000000 + '" lon="' +
        Math.floor(lng * 10000000) / 10000000 + '"><time>' +
        ISODateString(d) + '</time></trkpt>\n');

        prelat = lat;
        prelng = lng;
        ptimepre = ptime;
    }*/

}

/*
function makegpx() {
    var oFormObject = document.forms['togpx'];
    oFormObject.elements["gpxdata"].value = '<?xml version="1.0" encoding="UTF-8"' +
    ' ?><gpx xmlns="http://www.topografix.com/GPX/1/1"' +
    ' version="1.1" creator="RGGPSLOGGER" ' +
    'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
    'xmlns:tp1="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"' +
    ' xsi:schemaLocation="http://www.topografix.com/GPX/1/1 ' +
    'http://www.topografix.com/GPX/1/1/gpx.xsd ' +
    'http://www.garmin.com/xmlschemas/TrackPointExtension/v1 ' +
    'http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd">' +
    '<trk><trkseg>' + GPX.join('') + '</trkseg></trk></gpx>';
    oFormObject.elements["gpxfilename"].value = gpxname;
    oFormObject.submit();
}*/


function noLocation(p) {
    //$("#info").append("Error: "+p.message+"<br>");
    $("#location").html("Error finding gps: " + p.message + "<br>Retrying...");
    //alert('Error: ' +p.message);
    navigator.geolocation.clearWatch(watchid);
    watchid = navigator.geolocation.watchPosition(
      foundLocation,
      noLocation, {
          enableHighAccuracy: true,
          timeout: 3000,
          maximumAge: 10000
      });
}


// Reused code - copyright Moveable Type Scripts - retrieved May 4, 2010.
// http://www.movable-type.co.uk/scripts/latlong.html
// Under Creative Commons License http://creativecommons.org/licenses/by/3.0/

/*
function calculateDistance(lat1, lon1, lat2, lon2) {
    var R = 6371; // km
    var dLat = (lat2 - lat1).toRad();
    var dLon = (lon2 - lon1).toRad();
    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var d = R * c;
    return d;
}

Number.prototype.toRad = function () {
    return this * Math.PI / 180;
}

function ISODateString(d) {
    function pad(n) { return n < 10 ? '0' + n : n }
    return d.getUTCFullYear() + '-'
    + pad(d.getUTCMonth() + 1) + '-'
    + pad(d.getUTCDate()) + 'T'
    + pad(d.getUTCHours()) + ':'
    + pad(d.getUTCMinutes()) + ':'
    + pad(d.getUTCSeconds()) + 'Z';
}

function addzero(n) {
    return (n < 10) ? ("0" + n) : n;
}*/

function resend() {
    $("#buffer").html(buffer.length);
    if (buffer.length > 0) {

        //var posData = buffer.shift();

        var tmp = buffer.slice();
        buffer.length = 0;

        // send to server
        $.ajax({
            method: "post",
            url: server + '/save.php',
            data: {
                multiple: tmp,
                length: tmp.length
            },
            success: function (res) {
                if (res != "OK") {
                    //$("#console").append("Buffer: " + buffer.length + "<br>");
                    //$("#console").append("Retrying: " + tmp.length + "<br>");
                    $.each(tmp, function (point) {
                        buffer.push(tmp[point]);
                    });
                    //$("#console").append("Buffer: " + buffer.length + "<br>");
                } else {
                    //$("#console").append("Uploaded " + tmp.lenght + " points.<br>");
                }
            },
            error: function (xhr, status, error) {
                //$("#console").append("2Buffer: " + buffer.length + "<br>");
                //$("#console").append("2Retrying: " + tmp.length + "<br>");
                $.each(tmp, function (point) {
                    buffer.push(tmp[point]);
                });
                //$("#console").append("2Buffer: " + buffer.length + "<br>");
            }
        });
    }

    if (started != 0) {
        setTimeout(resend, 5000);
    }
}

function run() {

    //alert("run()");

    $("#id").change(function () {
        tunnus = $(this).val();
    });

    $("#server").change(function () {
        server = $(this).val();
    });

    $("#startgps").click(function () {
        //alert("click");
        if (startgps()) {
            cordova.plugins.backgroundMode.enable();
            $(this).attr("disabled", true);
            $("#stopgps").attr("disabled", false);
            resend();
        }
    });

    $("#stopgps").click(function () {
        $(this).attr("disabled", true);
        navigator.geolocation.clearWatch(watchid);
        cordova.plugins.backgroundMode.disable();
        started = 0;
        playing = 0;
        $("#location").html("Press Start tracking -button to start tracking.");
        $("#startgps").attr("disabled", false);
    });

    //$("#startlogger").click(function() {
    //  startlogger();
    //});

    //$("#getgpx").click(function() {
    //  makegpx();
    //});

}

function onResume() {
	// send to server
	$.ajax({
		method: "post",
		url: server + '/save.php',
		data: {
			c: tunnus,
			screen: "on"
		}
	});
}