package org.studyroom.web;

import java.io.*;
import fi.iki.elonen.NanoHTTPD.*;
import fi.iki.elonen.NanoWSD.*;
import fi.iki.elonen.NanoWSD.WebSocketFrame.*;

/**Handler for web socket messages. It incapsulate a web socket, but don't 
 * receive the first message, used by the server to select the right handler.
 * <br><br>
 * In subclasses never call methods of this class in the constructor,
 * instead use the {@code onOpen()} method to initialize data. */
public abstract class WebSocketHandler {
	private WebSocket socket;
	void setSocket(WebSocket s){
		socket=s;
		onOpen();
	}
	protected IHTTPSession getConnectionRequest(){
		return socket.getHandshakeRequest();
	}
	protected Session getSession(){
		return HTMLPage.getSession(socket.getHandshakeRequest());
	}
	protected CookieHandler getCookies(){
		return socket.getHandshakeRequest().getCookies();
	}
	protected boolean isOpen(){
		return socket.isOpen();
	}
	protected void close(CloseCode code, String reason, boolean initiatedByRemote){
		try{
			socket.close(code,reason,initiatedByRemote);
		} catch (IOException e){
			onException(e);
		}
	}
	protected void ping(byte[] payload){
		try{
			socket.ping(payload);
		} catch (IOException e){
			onException(e);
		}
	}
	protected void send(byte[] payload){
		try{
			socket.send(payload);
		} catch (IOException e){
			onException(e);
		}
	}
	protected void send(String payload){
		try{
			socket.send(payload);
		} catch (IOException e){
			onException(e);
		}
	}
	protected void send(WebSocketFrame frame){
		try{
			socket.sendFrame(frame);
		} catch (IOException e){
			onException(e);
		}
	}
    protected abstract void onOpen();
    protected abstract void onClose(CloseCode code, String reason, boolean initiatedByRemote);
    protected abstract void onMessage(WebSocketFrame message);
	protected void onPong(WebSocketFrame pong){
		if (getCookies().read(HTMLPage.SESSION_ID)!=null)
			getSession().access();
		ping(pong.getBinaryPayload());
	}
	protected void onException(IOException e){
		e.printStackTrace();
	}
}
