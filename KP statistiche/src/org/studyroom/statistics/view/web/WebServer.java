package org.studyroom.statistics.view.web;

import java.util.*;
import java.util.stream.*;
import fi.iki.elonen.router.*;

public class WebServer extends RouterNanoHTTPD {
	public WebServer(){
		super(80);
		addMappings();
	}
	/*@Override
	public Response serve(IHTTPSession s){
		String pag=s.getUri();
		Map<String,List<String>> par=s.getParameters();
		return NanoHTTPD.newFixedLengthResponse(Status.OK,"text/html",pag+"<br>"+par);
	}*/
	@Override
	public void addMappings(){
		//super.addMappings();
		addRoute("/home",Home.class);
		addRoute("/",Home.class);
		setNotFoundHandler(GeneralHandler.class);
	}
	public static class Home extends HTMLHandler {
		@Override
		public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
			return getHTMLResponse(uriResource+"<br>"+request.getUri()+"<br>Url parameters: "+urlParams+"<br>Query parameters: "+request.getParameters()+" | "+request.getParameters().entrySet().stream().map(e->e.getValue().size()+"").collect(Collectors.joining(" ")));
		}
	}
}
