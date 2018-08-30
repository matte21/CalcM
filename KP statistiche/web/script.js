//var kSessionID
var ws;
function createWebSocket(){
	ws=new WebSocket("ws://"+window.location.hostname+":82"); 
	ws.onmessage = function(e){
		document.getElementById("cont").innerHTML=e.data;
	}
	ws.onopen = function(){
		//ws.send(leggiCookie(kSessionID));
	}
	ws.onclose = function(){
		alert("connessione chiusa");	//XXX///////////////
		createWebSocket();
	}
	ws.onerror = function(){
		alert("errore nella connessione");	//XXX///////////////
		ws.onclose();
	}
}
function selectStatistic(st){
	ws.send("statistic="+st);
	alert(st);
}
function selectStudyRoom(sr){
	ws.send("studyRoom="+sr);
	alert(sr);
}

createWebSocket();