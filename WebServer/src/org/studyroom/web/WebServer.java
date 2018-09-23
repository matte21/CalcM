package org.studyroom.web;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import fi.iki.elonen.*;
import fi.iki.elonen.NanoWSD.*;
import fi.iki.elonen.NanoWSD.WebSocketFrame.*;
import fi.iki.elonen.router.*;

public class WebServer extends RouterNanoHTTPD {

	public static final int HTTP_PORT=80, WEB_SOCKET_PORT=82;
	private static WebServer instance;
	
	public static WebServer getInstance(){
		if (instance==null)
			instance=new WebServer(HTTP_PORT,WEB_SOCKET_PORT);
		return instance;
	}
	
	private final SessionManager sm;
	private final NanoWSD wsServer;
	private final List<Consumer<Session>> finalizers=new Vector<>();
	private final Map<String,Class<? extends WebSocketHandler>> wsHandlers=new ConcurrentHashMap<>();
	
	private WebServer(int httpPort, int wsPort){
		super(httpPort);
		addMappings();
		sm=new SessionManager(3600000,s->finalizers.forEach(f->f.accept(s)));
		wsServer=new NanoWSD(wsPort){
			@Override
			protected WebSocket openWebSocket(IHTTPSession request){
				return new WebSocketDispatcher(request);
			}
		};
	}
	
	SessionManager getSessionManager(){
		return sm;
	}
	
	public void addSessionFinalizer(Consumer<Session> finalizer){
		finalizers.add(finalizer);
	}
	
	public void removeSessionFinalizer(Consumer<Session> finalizer){
		finalizers.remove(finalizer);
	}
	
	@Override
	public void start(int timeout, boolean daemon) throws IOException {
		super.start(timeout,daemon);
		wsServer.start(timeout);
	}
	
	@Override
	public void addMappings(){
		setNotFoundHandler(NotFound.class);
	}
	
	public void addWebSocketMapping(String key, Class<? extends WebSocketHandler> h){
		wsHandlers.put(key,h);
	}
	
	public static class NotFound extends HTMLPage {
		@Override
		public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
			return ErrorHandler.DEFAULT.createErrorResponse(Response.Status.NOT_FOUND);
		}
	}
	
	
	private class WebSocketDispatcher extends WebSocket {
	
		private WebSocketHandler h;
		
		public WebSocketDispatcher(IHTTPSession handshakeRequest){
			super(handshakeRequest);
		}
		
		@Override
		protected void onOpen(){}
		
		@Override
		protected void onClose(CloseCode code, String reason, boolean initiatedByRemote){
			if (h!=null)
				h.onClose(code, reason, initiatedByRemote);
		}
		
		@Override
		protected void onMessage(WebSocketFrame message){
			if (h!=null)
				h.onMessage(message);
			else {
				String k=message.getTextPayload();
				if (wsHandlers.containsKey(k))
					try {
						h=wsHandlers.get(k).newInstance();
						h.setSocket(this);
						h.onOpen();
					} catch (InstantiationException|IllegalAccessException e){
						e.printStackTrace();
						try {
							close(CloseCode.InternalServerError,"",false);
						} catch (IOException ex){}
					}
				else {
					System.err.println("Socket dispatcher: unknown service: "+k);
					try {
						close(CloseCode.InvalidFramePayloadData,"Unknown service: "+k,false);
					} catch (IOException ex){}

				}
			}
		}
	
		@Override
		protected void onPong(WebSocketFrame pong){
			if (h!=null)
				h.onPong(pong);
		}
		
		@Override
		protected void onException(IOException e){
			if (h!=null)
				h.onException(e);
			else
				e.printStackTrace();
		}
	}
}
