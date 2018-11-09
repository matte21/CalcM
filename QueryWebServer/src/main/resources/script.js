"use strict";

// TODO 121 disable button after first click
// TODO 122 during a transition from one sub to another notifications should be disabled
// TODO 132 implement callbacks

var ws;
var lastSetOfMatchingRooms;
const httpBaseURL = "http://localhost:80/filters?"; 
const wsURL = "ws://localhost:82";
const newDivForResultsID = "filters_matching_rooms_div";
const resultsDisplayerParentID = "query_results_container";


window.addEventListener("load", registerHandlers);
window.addEventListener("close", closeWsConnection);

function registerHandlers(event) {
    const searchButtonID = "searchButton";
    document.getElementById(searchButtonID).addEventListener("click", onSearchButtonClick);	
}

function closeWsConnection() {
    if (ws) {
        ws.close();        
    }
}

function onSearchButtonClick(event) {
    stopWsNotifications();
    sendRequestWFilters();
}

function stopWsNotifications() {
    if (ws) {
        ws.send("stop");
    }    
}

function sendRequestWFilters() {
    // Prepare request URL with filters
    const filtersFieldSetID = "filtersFieldSet";
    var filtersFieldSet     = document.getElementById(filtersFieldSetID);
    if (validateFields() === false) {
        return;
    }
    var queryString = buildQueryStringFromFiltersFieldSet(filtersFieldSet);
    var requestURL  = httpBaseURL + queryString;

    // Issue AJAX request with filters
    var filteredRequest = new XMLHttpRequest();
    
    filteredRequest.onreadystatechange = function() {
        if (this.readyState == 4 && this.status != 200) {
            document.getElementById(resultsDisplayerParentID).innerHTML = "<p>" + this.statusText + "</p>";
        }
        if (this.readyState == 4 && this.status == 200) {
            lastSetOfMatchingRooms = JSON.parse(this.responseText);
            displayQueryResults(lastSetOfMatchingRooms);
            addNotificationsButton(queryString);
        }
    }

    filteredRequest.open("GET", requestURL, true);
    filteredRequest.send();
}

function displayQueryResults(roomsInResults, additionalHeaderMsg) {
    additionalHeaderMsg = additionalHeaderMsg || "";
    if (!roomsInResults || roomsInResults.length < 1) {
                // No rooms matching the request filters were found
                document.getElementById(resultsDisplayerParentID).innerHTML = '<div class="displayer" id="' + newDivForResultsID + '">'
                + '<h3 id="matching_filters_header">' + "There are no rooms matching the filters" + additionalHeaderMsg + '</h3>'
                + '</div>';
            } else {
                // At least one room matching the request filters was found
                var htmlWRooms = '<div class="displayer" id="' + newDivForResultsID + '">'
                + '<h3 id="matching_filters_header">' 
                + 'Study rooms matching the filters' + additionalHeaderMsg + '</h3>'
                + '<table class="rooms_table">'
                + '<thead>'
                + '<tr>'
                + '<th>Room name</th>'
                + '<th>Available Seats</th>'
                + '<th>Capacity</th>'
                + '<th>Address</th>'
                + '<th>State</th>'
                + '<th>State changes at:</th>'
                + '<th>University</th>'
                + '</tr>'
                + '</thead>'
                + '<tbody  id="filters_matching_rooms_tbody">';

                for (var i = 0; i < roomsInResults.length; i++) {
                    var aRoom = roomsInResults[i];
                    // slice removes timezone offset and seconds
                    var normalizedTransitionInstant = aRoom.nextTransitionInstant.slice(0, -9);
                    var dateAndTime = normalizedTransitionInstant.split("T");
                    htmlWRooms += '<tr>' 
                    + '<td>' + aRoom.roomID                          + '</td>'
                    + '<td>' + aRoom.availSeats                      + '</td>'
                    + '<td>' + aRoom.capacity                        + '</td>'
                    + '<td>' + aRoom.address                         + '</td>'
                    + '<td>' + aRoom.roomState                       + '</td>'
                    + '<td>' + dateAndTime[1] + " " + dateAndTime[0] + '</td>'
                    + '<td>' + aRoom.university                      + '</td>'
                    + '</tr>';
                }

                htmlWRooms += '</tbody>'
                + '</table>'
                + '</div>';

                document.getElementById(resultsDisplayerParentID).innerHTML = htmlWRooms;
            }
        }

var notifClickCallback;

function addNotificationsButton(queryString) {
    const htmlNotifButtonID = "notif_button";
    var htmlNotifButtonString
                    = '<button id="' + htmlNotifButtonID + '" type="button"> get notifications when results change </button>';
 
    document.getElementById(resultsDisplayerParentID).innerHTML += '<div id="notif_div">'
                                                            + htmlNotifButtonString
                                                            +'</div>';

    notifClickCallback = (function(queryString, htmlNotifButtonID) {
                            return function() { maybeOpenWsConnectionAndSendMsg(queryString, htmlNotifButtonID); }
                        })(queryString, htmlNotifButtonID);
    document.getElementById(htmlNotifButtonID).addEventListener("click", notifClickCallback);
}

function maybeOpenWsConnectionAndSendMsg(msg, htmlNotifButtonID) {
    // To avoid sending the same message multiple times if the user clicks the button more than once
    var htmlNotifButton = document.getElementById(htmlNotifButtonID);
    htmlNotifButton.removeEventListener("click", notifClickCallback);
    htmlNotifButton.parentNode.removeChild(htmlNotifButton);
    
    document.getElementById("matching_filters_header").innerHTML += " - real-time results";
    
    if (!ws) {
        openWsConnectionAndRegisterCallbacks(msg);
    } else {
        ws.send(msg);
    }
}

function openWsConnectionAndRegisterCallbacks(msg) {
    ws           = new WebSocket(wsURL);
    ws.onopen    = function() { ws.send("filters"); ws.send(msg); }
    ws.onmessage = function(msg) { updateResultsView(msg.data); } 
    ws.onerror   = function(err) { displayError(err); }
    ws.onclose   = function() { displayServerIsClosedMsg(); }
}

function updateResultsView(newResultsJSON) {
    console.log(newResultsJSON);

    var roomsToAddAndRemove = JSON.parse(newResultsJSON);
    var roomsToAdd          = roomsToAddAndRemove.toAdd;
    var roomsToRemove       = roomsToAddAndRemove.toRemove;

    // Remove rooms no longer in the results
    if (roomsToRemove && roomsToRemove.length > 0) {
        lastSetOfMatchingRooms = lastSetOfMatchingRooms.filter(function(aRoom) {
            for (var i = 0; i < roomsToRemove.length; i++) {
                if (aRoom.roomID == roomsToRemove[i].roomID) {
                    return false;
                }
            }
            return true;
        });
    }

    // Add new rooms
    if (roomsToAdd && roomsToAdd.length > 0) {
        lastSetOfMatchingRooms = lastSetOfMatchingRooms.concat(roomsToAdd);        
    }

    displayQueryResults(lastSetOfMatchingRooms, " - real-time results");

    window.alert("results changed!");
}

function displayErrorAndCloseTheWebSocketConnection(err) {
    var paragraph = document.createElement("P");
    var msg = document.createTextNode("The server experienced an error: " + err + ". No more updates will be received.");
    paragraph.appendChild(msg);
    document.getElementById(resultsDisplayerParentID).appendChild(paragraph);
    ws.close();
}

function displayServerIsClosedMsg() {
    var paragraph = document.createElement("P");
    var msg = document.createTextNode("The server closed the websocket connection. No more updates will be received.");
    paragraph.appendChild(msg);
    document.getElementById(resultsDisplayerParentID).appendChild(paragraph);
}

function buildQueryStringFromFiltersFieldSet(filtersFieldSet) {
    var queryString = "";
    var filters = filtersFieldSet.getElementsByTagName("input");

    Array.from(filters).forEach(function(filter) {
        var encodedFilter = encodeFilterForQueryString(filter);
        queryString += (encodedFilter == "" ? encodedFilter : encodedFilter + "&");
    });

    // slice strips off the last char, which is a spurius "&" 
    return queryString.slice(0, -1);
}

function encodeFilterForQueryString(filter) {
    var encodedFilter = "";
   
    switch (filter.type) {
        case "checkbox":
            encodedFilter += encodeCheckBoxForQueryString(filter);
            break;
        case "number":
            encodedFilter += encodeNumberForQueryString(filter);
            break;
    }

    return encodedFilter;
}

function encodeCheckBoxForQueryString(checkBoxFilter) {
    var encodedCheckBox = "";

    if (checkBoxFilter.checked) {
        encodedCheckBox = checkBoxFilter.name + "=" + checkBoxFilter.value; 
    }

    return encodedCheckBox;
}

function encodeNumberForQueryString(numberFilter) {
    var encodedNumber = "";

    if (numberFilter.value) {
        encodedNumber = numberFilter.name + "=" + numberFilter.value; 
    }

    return encodedNumber;
}


function validateFields() {
    var availSeats = document.getElementById("availSeats");
    
    if (availSeats.min && availSeats.value && availSeats.value < availSeats.min) {
        window.alert("Number of available seats must be bigger than " + availSeats.min);    
        return false;
    }
    if (availSeats.max && availSeats.value && availSeats.value > availSeats.max) {
        window.alert("Number of available seats must be smaller than " +  availSeats.max);
        return false;
    }
    if (!Number.isInteger(Number(availSeats.value))) {
        window.alert("Number of available seats must be an integer");
        return false;
    }

    return true;
}