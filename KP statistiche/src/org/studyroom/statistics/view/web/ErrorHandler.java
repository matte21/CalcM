package org.studyroom.statistics.view.web;

import fi.iki.elonen.*;
import fi.iki.elonen.NanoHTTPD.*;
import fi.iki.elonen.NanoHTTPD.Response.*;

public interface ErrorHandler {
	public Response createErrorResponse(IStatus error);
	public static final ErrorHandler DEFAULT=new DefaultErrorHandler();
	public static class DefaultErrorHandler implements ErrorHandler {
		@Override
		public Response createErrorResponse(IStatus error){
			return NanoHTTPD.newFixedLengthResponse(error,"text/plain","");
		}
	}
}
