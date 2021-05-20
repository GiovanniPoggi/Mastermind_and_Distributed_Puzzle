/**
* This Class manage the Graphics and the Request to Server that the Button and the User ask.
*/

/* Button to Register */
const addBtn = document.getElementById('addBtn');
/* Button to Login */
const updateBtn = document.getElementById('updateBtn');
/* Button to Login as a Guest */
const lazyAddBtn = document.getElementById('lazyAddBtn');
/* Button to Log Out */
const logOutBtn = document.getElementById('logOutBtn');

/* Fields of the User */
var name = "";
var username = "";
var password = "";
var email = "";
var isLogin = true;

/* Fields of the Token of the User */
var token = "";
var isTokenValid = false;

/* Boolean Field to Update Graphics */
var login = true;

var position0;
var position1;
var mousePos = 0;

/* Constant that represent the Type of the Request to Server */
const REQUEST_TO_VALID_USER_LOG_IN = 0;
const REQUEST_TO_CHECK_TOKEN = 1;
const REQUEST_TO_ADD_USER = 2;
const REQUEST_TO_LOG_OUT_USER = 6;

/* Listener into Register Button that Sign Up the User into the Database */
addBtn.addEventListener('click', (e) => {
    e.preventDefault();
    name = document.getElementById('registerName').value;
    username = document.getElementById('registerUsername').value;
    email = document.getElementById('registerEmail').value;
    password = document.getElementById('registerPassword').value;
    if (name != null && username != null && password != null && email != null && name != "" && username != "" && password != "" && email != "") {
        sendRequest(REQUEST_TO_ADD_USER);
        sendRequest(REQUEST_TO_CHECK_TOKEN);
        updateHTMLForUser();
    } else {
        alert("Name, Username or Password are INVALID!");
    }
});

/* Listener into Update Button that Sign In the User into the Global Puzzle */
updateBtn.addEventListener('click', (e) => {
    e.preventDefault();
    username = document.getElementById('loginUsername').value;
    password = document.getElementById('loginPassword').value;
    if (username != null && password != null && username != "" && password != "") {
        sendRequest(REQUEST_TO_VALID_USER_LOG_IN);
        updateHTMLForUser();
    } else {
        alert("Name, Username or Password INVALID!!");
    }
});

/* Listener into Lazy Login that Sign In the User as a Guest into the Global Puzzle */
lazyAddBtn.addEventListener('click', (e) => {
    e.preventDefault();
    username = document.getElementById('lazyUsername').value;
    if (username != null && username != "") {
        updateHTMLForUser();
    } else {
        alert("Enter at least a Username VALID please!");
    }
});

/* Method that send the request to Server to get the data of the User */
function sendRequest(kindOfRequest) {
    var xmlhttp = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    switch(kindOfRequest) {
      case REQUEST_TO_VALID_USER_LOG_IN:
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                token = xmlhttp.responseText;
                if (token != null && token != "") {
                    sendRequest(REQUEST_TO_CHECK_TOKEN);
                    isLogin = true;
                    sendRequest(REQUEST_MANAGE_ONLINE_USERS);
                } else {
                    alert("Name, Username or Password INVALID!!");
                    location.reload();
                }
            }
        };
        xmlhttp.open("POST", "http://localhost:8080/api/auth-api/login");
        xmlhttp.setRequestHeader("Content-Type", "application/json");
        xmlhttp.send(JSON.stringify({username: username, password: password}));
        break;
      case REQUEST_TO_CHECK_TOKEN:
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                isTokenValid = xmlhttp.responseText;
                if (isTokenValid == "admin") { //Login Admin
                    document.getElementById("adminView").style.display = "block";
                }
            }
        };
        xmlhttp.open("POST", "http://localhost:8080/api/auth-api/auth/role");
        xmlhttp.setRequestHeader("Authorization", token);
        xmlhttp.send();
        break;
      case REQUEST_TO_ADD_USER:
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                sendRequest(REQUEST_TO_VALID_USER_LOG_IN);
            }
        };
        xmlhttp.open("POST", "http://localhost:8080/api/auth-api/auth/signup");
        xmlhttp.setRequestHeader("Content-Type", "application/json");
        xmlhttp.send(JSON.stringify({name: name, username: username, email: email, password: password, role: "user"}));
        break;
      case REQUEST_TO_LOG_OUT_USER:
        xmlhttp.open("POST", "http://localhost:8080/api/auth-api/auth/????");
        xmlhttp.setRequestHeader("Authorization", token);
        xmlhttp.send();
        isLogin = false;
        sendRequest(REQUEST_MANAGE_ONLINE_USERS);
        break;
      default:
        alert('KIND OF REQUEST OUT OF RANGE!');
    }
}

/* Method that Update the View after Login/Register View */
function updateHTMLForUser() {
    document.getElementById("login").style.display = "none";
    document.getElementById("register").style.display = "none";
    document.getElementById("lazyLogin").style.display = "none";
}

/* Method that change the View between Login to Register */
function changeView() {
    if (login) {
        document.getElementById("login").style.display = "none";
        document.getElementById("register").style.display = "block";
        login = false;
    } else {
        document.getElementById("login").style.display = "block";
        document.getElementById("register").style.display = "none";
        login = true;
    }
}

/* Method that change the View between Login/Register to Lazy Login */
function changeViewLazy() {
    document.getElementById("login").style.display = "none";
    document.getElementById("register").style.display = "none";
    document.getElementById("lazyLogin").style.display = "block";
}

function swap(element_id) {
    if (position0 > -1) {
        var xmlhttp = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
        xmlhttp.open("POST", "http://localhost:8080/api/puzzle-api/swap");
        xmlhttp.setRequestHeader("Content-Type", "application/json");
        position1 = element_id;
        xmlhttp.send(JSON.stringify({position0: position0, position1: position1}));
        position0 = -1;
        position1 = -1;
    } else {
        position0 = element_id;
    }
}