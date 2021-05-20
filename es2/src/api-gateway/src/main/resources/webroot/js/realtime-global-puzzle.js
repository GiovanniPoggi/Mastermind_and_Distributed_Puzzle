/**
* JS File that manage all call to Server and the data into JSon Object from Server
*/
var current_puzzle_id = 1;
var private_chat = false;
var eventBus;
var end = false;
var isStarted = false;

function init() {
    registerHandlerForUpdateCurrentPuzzle();
};

/*
* Method that manage and display Cursor of other Players, Swap 2 pieces of the puzzle and add new Online User Cursor.
*/
function registerHandlerForUpdateCurrentPuzzle() {
    eventBus = new EventBus('http://localhost:9001/eventbus');
    eventBus.onopen = function () {
        eventBus.registerHandler('global_puzzle.' + current_puzzle_id, function (error, message) {
            if (!end) {
                var tmp = document.getElementById(message.body.position0).src;
                document.getElementById(message.body.position0).src = document.getElementById(message.body.position1).src;
                document.getElementById(message.body.position1).src = tmp;
            }
        });
        eventBus.registerHandler('end.' + current_puzzle_id, function (error, message) {
            alert("Risolto!");
            end = true;
        });
        eventBus.registerHandler('puzzle_users.' + current_puzzle_id, function (error, message) {
            console.log(message.body);
            var msg = JSON.parse(message.body);

            if (msg.username != username) {
                document.getElementById(msg.username).style.top = parseInt(msg.positionY)+'px';
                document.getElementById(msg.username).style.left = parseInt(msg.positionX)+'px';
                var temp2 = msg.username;
                temp2 = temp2.concat(temp2);
                document.getElementById(temp2).style.top = (parseInt(msg.positionY)+20)+'px';
                document.getElementById(temp2).style.left = parseInt(msg.positionX)+'px';
            }
        });
        eventBus.registerHandler('newOnlineUser.' + current_puzzle_id, function (error, message) {
            if (message.body.username != username) {
                var us = message.body.username;
                createCursor(us);
            }
        });
    }
};

function play() {
    document.getElementById("start").style.display = "none";
    document.getElementById("join").style.display = "none";
    document.getElementById("toHide").style.display = "none";
    isStarted = true;

    var imgUrl = document.getElementById('links').value;
    var e = document.getElementById("gridCols");
    var dimensionCols = e.options[e.selectedIndex].value;

    var xmlhttp = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState == 4) {
            if (xmlhttp.status == 200) {
                var pieces = JSON.parse(this.responseText);

                document.getElementById(pieces.position0).src = "data:image/jpg;base64," + pieces.image0;
                document.getElementById(pieces.position1).src = "data:image/jpg;base64," + pieces.image1;
                document.getElementById(pieces.position2).src = "data:image/jpg;base64," + pieces.image2;
                document.getElementById(pieces.position3).src = "data:image/jpg;base64," + pieces.image3;
                document.getElementById(pieces.position4).src = "data:image/jpg;base64," + pieces.image4;
                document.getElementById(pieces.position5).src = "data:image/jpg;base64," + pieces.image5;
                document.getElementById(pieces.position6).src = "data:image/jpg;base64," + pieces.image6;
                document.getElementById(pieces.position7).src = "data:image/jpg;base64," + pieces.image7;
                document.getElementById(pieces.position8).src = "data:image/jpg;base64," + pieces.image8;
                document.getElementById(pieces.position9).src = "data:image/jpg;base64," + pieces.image9;
                document.getElementById(pieces.position10).src = "data:image/jpg;base64," + pieces.image10;
                document.getElementById(pieces.position11).src = "data:image/jpg;base64," + pieces.image11;
                document.getElementById(pieces.position12).src = "data:image/jpg;base64," + pieces.image12;
                document.getElementById(pieces.position13).src = "data:image/jpg;base64," + pieces.image13;
                document.getElementById(pieces.position14).src = "data:image/jpg;base64," + pieces.image14;
                document.getElementById(pieces.position15).src = "data:image/jpg;base64," + pieces.image15;

            } else {
                //ERROR
            }
        }
    };

    xmlhttp.open("POST", "http://localhost:8080/api/puzzle-api/play");
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    xmlhttp.send(JSON.stringify({imgUrl: imgUrl, dimensionCols: dimensionCols, username: username}));
};

function join() {
    document.getElementById("start").style.display = "none";
    document.getElementById("join").style.display = "none";
    document.getElementById("toHide").style.display = "none";
    isStarted = true;

    var e = document.getElementById("gridCols");
    var dimensionCols = e.options[e.selectedIndex].value;

    var xmlhttp = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState == 4) {
            if (xmlhttp.status == 200) {
                var pieces = JSON.parse(this.responseText);
                //document.getElementById(pieces.position0).src = "data:image/jpg;base64," + pieces.image0;

                document.getElementById(pieces.position0).src = "data:image/jpg;base64," + pieces.image0;
                document.getElementById(pieces.position1).src = "data:image/jpg;base64," + pieces.image1;
                document.getElementById(pieces.position2).src = "data:image/jpg;base64," + pieces.image2;
                document.getElementById(pieces.position3).src = "data:image/jpg;base64," + pieces.image3;
                document.getElementById(pieces.position4).src = "data:image/jpg;base64," + pieces.image4;
                document.getElementById(pieces.position5).src = "data:image/jpg;base64," + pieces.image5;
                document.getElementById(pieces.position6).src = "data:image/jpg;base64," + pieces.image6;
                document.getElementById(pieces.position7).src = "data:image/jpg;base64," + pieces.image7;
                document.getElementById(pieces.position8).src = "data:image/jpg;base64," + pieces.image8;
                document.getElementById(pieces.position9).src = "data:image/jpg;base64," + pieces.image9;
                document.getElementById(pieces.position10).src = "data:image/jpg;base64," + pieces.image10;
                document.getElementById(pieces.position11).src = "data:image/jpg;base64," + pieces.image11;
                document.getElementById(pieces.position12).src = "data:image/jpg;base64," + pieces.image12;
                document.getElementById(pieces.position13).src = "data:image/jpg;base64," + pieces.image13;
                document.getElementById(pieces.position14).src = "data:image/jpg;base64," + pieces.image14;
                document.getElementById(pieces.position15).src = "data:image/jpg;base64," + pieces.image15;

                var res = pieces.onlineUsers;
                //res = res.split(", ");
                for (var i=0; i<res.length; i++) {
                    console.log(res[i]);
                    if (res[i] != username) {
                        createCursor(res[i]);
                    }
                }

            } else {
                //ERROR
            }
        }
    };

    xmlhttp.open("POST", "http://localhost:8080/api/puzzle-api/join");
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    xmlhttp.send(JSON.stringify({username: username}));
};

function createCursor(user) {
    document.getElementById("onlineUsers").innerHTML += "<img id=\""+user+"\" style=\"position:absolute;left:0px;top:0px\" width=\"20\" height=\"21\" src=\"https://i.imgur.com/qI8vSnb.png\">";
    var temp = user.concat(user);
    document.getElementById("onlineUsers").innerHTML += "<span id=\""+temp+"\" style=\"position:absolute;left:0px;top:0px;color:white;font-size:12px\">"+user+"</span>";
}

function mouse_position() {

if (isStarted) {
    document.onmousemove = handleMouseMove;
    function handleMouseMove(event) {
        var eventDoc, doc, body;

        event = event || window.event; // IE-ism

        // If pageX/Y aren't available and clientX/Y are,
        // calculate pageX/Y - logic taken from jQuery.
        // (This is to support old IE)
        if (event.pageX == null && event.clientX != null) {
            eventDoc = (event.target && event.target.ownerDocument) || document;
            doc = eventDoc.documentElement;
            body = eventDoc.body;

            event.pageX = event.clientX +
              (doc && doc.scrollLeft || body && body.scrollLeft || 0) -
              (doc && doc.clientLeft || body && body.clientLeft || 0);
            event.pageY = event.clientY +
              (doc && doc.scrollTop  || body && body.scrollTop  || 0) -
              (doc && doc.clientTop  || body && body.clientTop  || 0 );
        }
        // Use event.pageX / event.pageY here
        eventBus.publish('puzzle_users.' + current_puzzle_id, JSON.stringify({positionX: event.pageX, positionY: event.pageY, username: username}));
    }
    }
}