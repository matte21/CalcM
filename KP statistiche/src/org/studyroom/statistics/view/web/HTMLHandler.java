package org.studyroom.statistics.view.web;

import java.util.*;
import java.util.stream.*;
import fi.iki.elonen.*;
import fi.iki.elonen.NanoHTTPD.*;
import fi.iki.elonen.NanoHTTPD.Response.*;
import fi.iki.elonen.router.RouterNanoHTTPD.*;

/**Base class for every HTML page.<br>
 * Override the method named as a request method to make a response to requests of that type.
 */
public abstract class HTMLHandler implements UriResponder {
	private static final Map<IStatus,ErrorHandler> errorHandlers=new HashMap<>();
	private static final List<String> methods=Arrays.asList("get","post","put","delete");
	public static void addErrorHandler(ErrorHandler h, IStatus...st){
		for (IStatus s : st)
			errorHandlers.put(s,h);
	}
	protected Response getErrorResponse(IStatus error){
		return errorHandlers.getOrDefault(error,ErrorHandler.DEFAULT).createErrorResponse(error);
	}
	protected Response getHTMLResponse(String html){
		return NanoHTTPD.newFixedLengthResponse(Status.OK,"text/html",html);
	}
	@Override
	public Response delete(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		return getErrorResponse(Status.METHOD_NOT_ALLOWED);
	}
	@Override
	public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		return getErrorResponse(Status.METHOD_NOT_ALLOWED);
	}
	@Override
	public Response other(String method, UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		Response r;
		switch (method){
		case "HEAD":
			r=get(uriResource,urlParams,request);
			Response rh=NanoHTTPD.newFixedLengthResponse(r.getStatus(),r.getMimeType(),"");
			return rh;
		case "OPTIONS":
			String ml=Arrays.stream(getClass().getDeclaredMethods()).filter(m->methods.contains(m.getName())).map(m->m.getName().toUpperCase()).collect(Collectors.joining(", "));
			if (ml.contains("GET"))
				ml+=", HEAD";
			ml+=", OPTIONS";
			r=NanoHTTPD.newFixedLengthResponse(Status.OK,"text/plain","");
			r.addHeader("Allow",ml);
			return r;
		default:
			return getErrorResponse(Status.METHOD_NOT_ALLOWED);
		}
	}
	@Override
	public Response post(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		return getErrorResponse(Status.METHOD_NOT_ALLOWED);
	}
	@Override
	public Response put(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		return getErrorResponse(Status.METHOD_NOT_ALLOWED);
	}
}
