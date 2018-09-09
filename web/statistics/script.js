var ws;
function createWebSocket(){
	ws=new WebSocket("ws://"+window.location.hostname+":82"); 
	ws.onopen=function(){
		ws.send("statistics");
	}
	ws.onmessage=function(e){
		document.getElementById("cont").innerHTML=e.data;
	}
	ws.onclose=createWebSocket;
}
function selectStatistic(st){
	ws.send("statistic="+st);
}
function selectStudyRoom(sr){
	ws.send("studyRoom="+sr);
}
function selectVisualization(v){
	ws.send("visualization="+v);
}

createWebSocket();