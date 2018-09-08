package org.studyroom.statistics.view.web;

import java.io.*;
import java.util.*;
import fi.iki.elonen.*;
import fi.iki.elonen.router.*;
import org.studyroom.statistics.viewmodel.*;

public class WebServer extends RouterNanoHTTPD {
	private static final int HTTP_PORT=80,WEB_SOCKET_PORT=82;
	static final String VIEW_MODEL_KEY="vm";
	private static Class<? extends IMainViewModel> vmClass;
	private static WebServer instance;
	public static WebServer getInstance(){
		if (instance==null)
			instance=new WebServer(HTTP_PORT,WEB_SOCKET_PORT);
		return instance;
	}
	public static void setViewModelClass(Class<? extends IMainViewModel> c){
		vmClass=c;
	}
	public static IMainViewModel newViewModel(){
		try{
			return vmClass.newInstance();
		} catch (Exception e){
			throw new Error(e);
		}
	}
	
	private final SessionManager sm;
	private final NanoWSD wsServer;
	private WebServer(int httpPort, int wsPort){
		super(httpPort);
		addMappings();
		sm=new SessionManager(3600000,s->((IMainViewModel)s.get(VIEW_MODEL_KEY)).unbind());
		wsServer=new NanoWSD(wsPort){
			@Override
			protected WebSocket openWebSocket(IHTTPSession handshake){
				return new GraphicPage.Socket(handshake);
			}
		};
	}
	SessionManager getSessionManager(){
		return sm;
	}
	@Override
	public void start(int timeout, boolean daemon) throws IOException {
		super.start(timeout,daemon);
		wsServer.start(timeout);
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
		//addRoute("/home",Home.class);
		addRoute("/",GraphicPage.class);
		addRoute("/rsc/.*",StaticPageHandler.class,new File("web"));
		//setNotFoundHandler(GeneralHandler.class);
		setNotFoundHandler(NotFound.class);
	}
	/*public static class Home extends HTMLHandler {
		@Override
		public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
			return getHTMLResponse(uriResource+"<br>"+request.getUri()+"<br>Url parameters: "+urlParams+"<br>Query parameters: "+request.getParameters()+" | "+request.getParameters().entrySet().stream().map(e->e.getValue().size()+"").collect(Collectors.joining(" ")));
		}
	}*/
	public static class NotFound extends HTMLPage {
		@Override
		public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
			return ErrorHandler.DEFAULT.createErrorResponse(Response.Status.NOT_FOUND);
		}
	}
}
