///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
////package javax.servlet.http;
//import java.io.*;
//import java.lang.reflect.*;
//import java.text.*;
//import java.util.*;
//import javax.servlet.*;
//
///**
// * Provides an abstract class to be subclassed to create an HTTP servlet suitable for a Web site. A subclass of <code>HttpServlet</code> must override at least one method, usually one of these:
// *
// * <ul>
// * <li><code>doGet</code>, if the servlet supports HTTP GET requests
// * <li><code>doPost</code>, for HTTP POST requests
// * <li><code>doPut</code>, for HTTP PUT requests
// * <li><code>doDelete</code>, for HTTP DELETE requests
// * <li><code>init</code> and <code>destroy</code>, to manage resources that are held for the life of the servlet
// * <li><code>getServletInfo</code>, which the servlet uses to provide information about itself
// * </ul>
// *
// * <p>
// * There's almost no reason to override the <code>service</code> method. <code>service</code> handles standard HTTP requests by dispatching them to the handler methods for each HTTP request type (the
// * <code>do</code><i>XXX</i> methods listed above).
// *
// * <p>
// * Likewise, there's almost no reason to override the <code>doOptions</code> and <code>doTrace</code> methods.
// * 
// * <p>
// * Servlets typically run on multithreaded servers, so be aware that a servlet must handle concurrent requests and be careful to synchronize access to shared resources. Shared resources include
// * in-memory data such as instance or class variables and external objects such as files, database connections, and network connections. See the
// * <a href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html"> Java Tutorial on Multithreaded Programming</a> for more information on handling multiple threads in a Java program.
// *
// * @author Various
// * @version $Version$
// */
//public abstract class HttpServlet extends GenericServlet implements java.io.Serializable {
//	private static final String METHOD_DELETE="DELETE";
//	private static final String METHOD_HEAD="HEAD";
//	private static final String METHOD_GET="GET";
//	private static final String METHOD_OPTIONS="OPTIONS";
//	private static final String METHOD_POST="POST";
//	private static final String METHOD_PUT="PUT";
//	private static final String METHOD_TRACE="TRACE";
//	private static final String HEADER_IFMODSINCE="If-Modified-Since";
//	private static final String HEADER_LASTMOD="Last-Modified";
//	private static final String LSTRING_FILE="javax.servlet.http.LocalStrings";
//	private static ResourceBundle lStrings=ResourceBundle.getBundle(LSTRING_FILE);
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a GET request.
//	 *
//	 * <p>
//	 * Overriding this method to support a GET request also automatically supports an HTTP HEAD request. A HEAD request is a GET request that returns no body in the response, only the request header
//	 * fields.
//	 *
//	 * <p>
//	 * When overriding this method, read the request data, write the response headers, get the response's writer or output stream object, and finally, write the response data. It's best to include
//	 * content type and encoding. When using a <code>PrintWriter</code> object to return the response, set the content type before accessing the <code>PrintWriter</code> object.
//	 *
//	 * <p>
//	 * The servlet container must write the headers before committing the response, because in HTTP the headers must be sent before the response body.
//	 *
//	 * <p>
//	 * Where possible, set the Content-Length header (with the {@link javax.servlet.ServletResponse#setContentLength} method), to allow the servlet container to use a persistent connection to return
//	 * its response to the client, improving performance. The content length is automatically set if the entire response fits inside the response buffer.
//	 *
//	 * <p>
//	 * When using HTTP 1.1 chunked encoding (which means that the response has a Transfer-Encoding header), do not set the Content-Length header.
//	 *
//	 * <p>
//	 * The GET method should be safe, that is, without any side effects for which users are held responsible. For example, most form queries have no side effects. If a client request is intended to
//	 * change stored data, the request should use some other HTTP method.
//	 *
//	 * <p>
//	 * The GET method should also be idempotent, meaning that it can be safely repeated. Sometimes making a method safe also makes it idempotent. For example, repeating queries is both safe and
//	 * idempotent, but buying a product online or modifying data is neither safe nor idempotent.
//	 *
//	 * <p>
//	 * If the request is incorrectly formatted, <code>doGet</code> returns an HTTP "Bad Request" message.
//	 *
//	 * @param req
//	 *            an {@link HttpServletRequest} object that contains the request the client has made of the servlet
//	 *
//	 * @param resp
//	 *            an {@link HttpServletResponse} object that contains the response the servlet sends to the client
//	 * 
//	 * @exception IOException
//	 *                if an input or output error is detected when the servlet handles the GET request
//	 *
//	 * @exception ServletException
//	 *                if the request for the GET could not be handled
//	 * 
//	 * @see javax.servlet.ServletResponse#setContentType
//	 */
//	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		String protocol=req.getProtocol();
//		String msg=lStrings.getString("http.method_get_not_supported");
//		if (protocol.endsWith("1.1")){
//			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,msg);
//		} else{
//			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,msg);
//		}
//	}
//	/**
//	 * Returns the time the <code>HttpServletRequest</code> object was last modified, in milliseconds since midnight January 1, 1970 GMT. If the time is unknown, this method returns a negative number
//	 * (the default).
//	 *
//	 * <p>
//	 * Servlets that support HTTP GET requests and can quickly determine their last modification time should override this method. This makes browser and proxy caches work more effectively, reducing
//	 * the load on server and network resources.
//	 *
//	 * @param req
//	 *            the <code>HttpServletRequest</code> object that is sent to the servlet
//	 *
//	 * @return a <code>long</code> integer specifying the time the <code>HttpServletRequest</code> object was last modified, in milliseconds since midnight, January 1, 1970 GMT, or -1 if the time is
//	 *         not known
//	 */
//	protected long getLastModified(HttpServletRequest req){
//		return -1;
//	}
//	/**
//	 * <p>
//	 * Receives an HTTP HEAD request from the protected <code>service</code> method and handles the request. The client sends a HEAD request when it wants to see only the headers of a response, such
//	 * as Content-Type or Content-Length. The HTTP HEAD method counts the output bytes in the response to set the Content-Length header accurately.
//	 *
//	 * <p>
//	 * If you override this method, you can avoid computing the response body and just set the response headers directly to improve performance. Make sure that the <code>doHead</code> method you write
//	 * is both safe and idempotent (that is, protects itself from being called multiple times for one HTTP HEAD request).
//	 *
//	 * <p>
//	 * If the HTTP HEAD request is incorrectly formatted, <code>doHead</code> returns an HTTP "Bad Request" message.
//	 *
//	 * @param req
//	 *            the request object that is passed to the servlet
//	 *
//	 * @param resp
//	 *            the response object that the servlet uses to return the headers to the clien
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs
//	 *
//	 * @exception ServletException
//	 *                if the request for the HEAD could not be handled
//	 */
//	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		NoBodyResponse response=new NoBodyResponse(resp);
//		doGet(req,response);
//		response.setContentLength();
//	}
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a POST request.
//	 *
//	 * The HTTP POST method allows the client to send data of unlimited length to the Web server a single time and is useful when posting information such as credit card numbers.
//	 *
//	 * <p>
//	 * When overriding this method, read the request data, write the response headers, get the response's writer or output stream object, and finally, write the response data. It's best to include
//	 * content type and encoding. When using a <code>PrintWriter</code> object to return the response, set the content type before accessing the <code>PrintWriter</code> object.
//	 *
//	 * <p>
//	 * The servlet container must write the headers before committing the response, because in HTTP the headers must be sent before the response body.
//	 *
//	 * <p>
//	 * Where possible, set the Content-Length header (with the {@link javax.servlet.ServletResponse#setContentLength} method), to allow the servlet container to use a persistent connection to return
//	 * its response to the client, improving performance. The content length is automatically set if the entire response fits inside the response buffer.
//	 *
//	 * <p>
//	 * When using HTTP 1.1 chunked encoding (which means that the response has a Transfer-Encoding header), do not set the Content-Length header.
//	 *
//	 * <p>
//	 * This method does not need to be either safe or idempotent. Operations requested through POST can have side effects for which the user can be held accountable, for example, updating stored data
//	 * or buying items online.
//	 *
//	 * <p>
//	 * If the HTTP POST request is incorrectly formatted, <code>doPost</code> returns an HTTP "Bad Request" message.
//	 *
//	 *
//	 * @param req
//	 *            an {@link HttpServletRequest} object that contains the request the client has made of the servlet
//	 *
//	 * @param resp
//	 *            an {@link HttpServletResponse} object that contains the response the servlet sends to the client
//	 * 
//	 * @exception IOException
//	 *                if an input or output error is detected when the servlet handles the request
//	 *
//	 * @exception ServletException
//	 *                if the request for the POST could not be handled
//	 *
//	 * @see javax.servlet.ServletOutputStream
//	 * @see javax.servlet.ServletResponse#setContentType
//	 */
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		String protocol=req.getProtocol();
//		String msg=lStrings.getString("http.method_post_not_supported");
//		if (protocol.endsWith("1.1")){
//			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,msg);
//		} else{
//			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,msg);
//		}
//	}
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a PUT request.
//	 *
//	 * The PUT operation allows a client to place a file on the server and is similar to sending a file by FTP.
//	 *
//	 * <p>
//	 * When overriding this method, leave intact any content headers sent with the request (including Content-Length, Content-Type, Content-Transfer-Encoding, Content-Encoding, Content-Base,
//	 * Content-Language, Content-Location, Content-MD5, and Content-Range). If your method cannot handle a content header, it must issue an error message (HTTP 501 - Not Implemented) and discard the
//	 * request. For more information on HTTP 1.1, see RFC 2616 <a href="http://www.ietf.org/rfc/rfc2616.txt"></a>.
//	 *
//	 * <p>
//	 * This method does not need to be either safe or idempotent. Operations that <code>doPut</code> performs can have side effects for which the user can be held accountable. When using this method,
//	 * it may be useful to save a copy of the affected URL in temporary storage.
//	 *
//	 * <p>
//	 * If the HTTP PUT request is incorrectly formatted, <code>doPut</code> returns an HTTP "Bad Request" message.
//	 *
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 * @param resp
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the PUT request
//	 *
//	 * @exception ServletException
//	 *                if the request for the PUT cannot be handled
//	 */
//	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		String protocol=req.getProtocol();
//		String msg=lStrings.getString("http.method_put_not_supported");
//		if (protocol.endsWith("1.1")){
//			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,msg);
//		} else{
//			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,msg);
//		}
//	}
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a DELETE request.
//	 *
//	 * The DELETE operation allows a client to remove a document or Web page from the server.
//	 * 
//	 * <p>
//	 * This method does not need to be either safe or idempotent. Operations requested through DELETE can have side effects for which users can be held accountable. When using this method, it may be
//	 * useful to save a copy of the affected URL in temporary storage.
//	 *
//	 * <p>
//	 * If the HTTP DELETE request is incorrectly formatted, <code>doDelete</code> returns an HTTP "Bad Request" message.
//	 *
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 *
//	 * @param resp
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the DELETE request
//	 *
//	 * @exception ServletException
//	 *                if the request for the DELETE cannot be handled
//	 */
//	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		String protocol=req.getProtocol();
//		String msg=lStrings.getString("http.method_delete_not_supported");
//		if (protocol.endsWith("1.1")){
//			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,msg);
//		} else{
//			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,msg);
//		}
//	}
//	private static Method[] getAllDeclaredMethods(Class c){
//		if (c.equals(javax.servlet.http.HttpServlet.class)){
//			return null;
//		}
//		Method[] parentMethods=getAllDeclaredMethods(c.getSuperclass());
//		Method[] thisMethods=c.getDeclaredMethods();
//		if ((parentMethods!=null)&&(parentMethods.length>0)){
//			Method[] allMethods=new Method[parentMethods.length+thisMethods.length];
//			System.arraycopy(parentMethods,0,allMethods,0,parentMethods.length);
//			System.arraycopy(thisMethods,0,allMethods,parentMethods.length,thisMethods.length);
//			thisMethods=allMethods;
//		}
//		return thisMethods;
//	}
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a OPTIONS request.
//	 *
//	 * The OPTIONS request determines which HTTP methods the server supports and returns an appropriate header. For example, if a servlet overrides <code>doGet</code>, this method returns the
//	 * following header:
//	 *
//	 * <p>
//	 * <code>Allow: GET, HEAD, TRACE, OPTIONS</code>
//	 *
//	 * <p>
//	 * There's no need to override this method unless the servlet implements new HTTP methods, beyond those implemented by HTTP 1.1.
//	 *
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 * @param resp
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the OPTIONS request
//	 *
//	 * @exception ServletException
//	 *                if the request for the OPTIONS cannot be handled
//	 */
//	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		Method[] methods=getAllDeclaredMethods(this.getClass());
//		boolean ALLOW_GET=false;
//		boolean ALLOW_HEAD=false;
//		boolean ALLOW_POST=false;
//		boolean ALLOW_PUT=false;
//		boolean ALLOW_DELETE=false;
//		boolean ALLOW_TRACE=true;
//		boolean ALLOW_OPTIONS=true;
//		for (int i=0;i<methods.length;i++){
//			Method m=methods[i];
//			if (m.getName().equals("doGet")){
//				ALLOW_GET=true;
//				ALLOW_HEAD=true;
//			}
//			if (m.getName().equals("doPost"))
//				ALLOW_POST=true;
//			if (m.getName().equals("doPut"))
//				ALLOW_PUT=true;
//			if (m.getName().equals("doDelete"))
//				ALLOW_DELETE=true;
//		}
//		String allow=null;
//		if (ALLOW_GET)
//			if (allow==null)
//				allow=METHOD_GET;
//		if (ALLOW_HEAD)
//			if (allow==null)
//				allow=METHOD_HEAD;
//			else
//				allow+=", "+METHOD_HEAD;
//		if (ALLOW_POST)
//			if (allow==null)
//				allow=METHOD_POST;
//			else
//				allow+=", "+METHOD_POST;
//		if (ALLOW_PUT)
//			if (allow==null)
//				allow=METHOD_PUT;
//			else
//				allow+=", "+METHOD_PUT;
//		if (ALLOW_DELETE)
//			if (allow==null)
//				allow=METHOD_DELETE;
//			else
//				allow+=", "+METHOD_DELETE;
//		if (ALLOW_TRACE)
//			if (allow==null)
//				allow=METHOD_TRACE;
//			else
//				allow+=", "+METHOD_TRACE;
//		if (ALLOW_OPTIONS)
//			if (allow==null)
//				allow=METHOD_OPTIONS;
//			else
//				allow+=", "+METHOD_OPTIONS;
//		resp.setHeader("Allow",allow);
//	}
//	/**
//	 * Called by the server (via the <code>service</code> method) to allow a servlet to handle a TRACE request.
//	 *
//	 * A TRACE returns the headers sent with the TRACE request to the client, so that they can be used in debugging. There's no need to override this method.
//	 *
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 * @param resp
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the TRACE request
//	 *
//	 * @exception ServletException
//	 *                if the request for the TRACE cannot be handled
//	 */
//	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		int responseLength;
//		String CRLF="\r\n";
//		String responseString="TRACE "+req.getRequestURI()+" "+req.getProtocol();
//		Enumeration reqHeaderEnum=req.getHeaderNames();
//		while (reqHeaderEnum.hasMoreElements()){
//			String headerName=(String)reqHeaderEnum.nextElement();
//			responseString+=CRLF+headerName+": "+req.getHeader(headerName);
//		}
//		responseString+=CRLF;
//		responseLength=responseString.length();
//		resp.setContentType("message/http");
//		resp.setContentLength(responseLength);
//		ServletOutputStream out=resp.getOutputStream();
//		out.print(responseString);
//		out.close();
//		return;
//	}
//	/**
//	 * Receives standard HTTP requests from the public <code>service</code> method and dispatches them to the <code>do</code><i>XXX</i> methods defined in this class. This method is an HTTP-specific
//	 * version of the {@link javax.servlet.Servlet#service} method. There's no need to override this method.
//	 *
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 * @param resp
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the HTTP request
//	 *
//	 * @exception ServletException
//	 *                if the HTTP request cannot be handled
//	 * 
//	 * @see javax.servlet.Servlet#service
//	 */
//	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//		String method=req.getMethod();
//		if (method.equals(METHOD_GET)){
//			long lastModified=getLastModified(req);
//			if (lastModified==-1){
//				// servlet doesn't support if-modified-since, no reason
//				// to go through further expensive logic
//				doGet(req,resp);
//			} else{
//				long ifModifiedSince=req.getDateHeader(HEADER_IFMODSINCE);
//				if (ifModifiedSince<(lastModified/1000*1000)){
//					// If the servlet mod time is later, call doGet()
//					// Round down to the nearest second for a proper compare
//					// A ifModifiedSince of -1 will always be less
//					maybeSetLastModified(resp,lastModified);
//					doGet(req,resp);
//				} else{
//					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//				}
//			}
//		} else if (method.equals(METHOD_HEAD)){
//			long lastModified=getLastModified(req);
//			maybeSetLastModified(resp,lastModified);
//			doHead(req,resp);
//		} else if (method.equals(METHOD_POST)){
//			doPost(req,resp);
//		} else if (method.equals(METHOD_PUT)){
//			doPut(req,resp);
//		} else if (method.equals(METHOD_DELETE)){
//			doDelete(req,resp);
//		} else if (method.equals(METHOD_OPTIONS)){
//			doOptions(req,resp);
//		} else if (method.equals(METHOD_TRACE)){
//			doTrace(req,resp);
//		} else{
//			//
//			// Note that this means NO servlet supports whatever
//			// method was requested, anywhere on this server.
//			//
//			String errMsg=lStrings.getString("http.method_not_implemented");
//			Object[] errArgs=new Object[1];
//			errArgs[0]=method;
//			errMsg=MessageFormat.format(errMsg,errArgs);
//			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,errMsg);
//		}
//	}
//	/*
//	 * Sets the Last-Modified entity header field, if it has not already been set and if the value is meaningful. Called before doGet, to ensure that headers are set before response data is written. A
//	 * subclass might have set this header already, so we check.
//	 */
//	private void maybeSetLastModified(HttpServletResponse resp, long lastModified){
//		if (resp.containsHeader(HEADER_LASTMOD))
//			return;
//		if (lastModified>=0)
//			resp.setDateHeader(HEADER_LASTMOD,lastModified);
//	}
//	/**
//	 * Dispatches client requests to the protected <code>service</code> method. There's no need to override this method.
//	 * 
//	 * @param req
//	 *            the {@link HttpServletRequest} object that contains the request the client made of the servlet
//	 *
//	 * @param res
//	 *            the {@link HttpServletResponse} object that contains the response the servlet returns to the client
//	 *
//	 * @exception IOException
//	 *                if an input or output error occurs while the servlet is handling the HTTP request
//	 *
//	 * @exception ServletException
//	 *                if the HTTP request cannot be handled
//	 * 
//	 * @see javax.servlet.Servlet#service
//	 */
//	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException{
//		HttpServletRequest request;
//		HttpServletResponse response;
//		try{
//			request=(HttpServletRequest)req;
//			response=(HttpServletResponse)res;
//		} catch (ClassCastException e){
//			throw new ServletException("non-HTTP request or response");
//		}
//		service(request,response);
//	}
//}
//
///*
// * A response wrapper for use in (dumb) "HEAD" support. This just swallows that body, counting the bytes in order to set the content length appropriately. All other methods delegate to the wrapped
// * HTTP Servlet Response object.
// */
//// file private
//class NoBodyResponse extends HttpServletResponseWrapper {
//	private NoBodyOutputStream noBody;
//	private PrintWriter writer;
//	private boolean didSetContentLength;
//	// file private
//	NoBodyResponse(HttpServletResponse r){
//		super(r);
//		noBody=new NoBodyOutputStream();
//	}
//	// file private
//	void setContentLength(){
//		if (!didSetContentLength)
//			super.setContentLength(noBody.getContentLength());
//	}
//	// SERVLET RESPONSE interface methods
//	public void setContentLength(int len){
//		super.setContentLength(len);
//		didSetContentLength=true;
//	}
//	public ServletOutputStream getOutputStream() throws IOException{
//		return noBody;
//	}
//	public PrintWriter getWriter() throws UnsupportedEncodingException{
//		if (writer==null){
//			OutputStreamWriter w;
//			w=new OutputStreamWriter(noBody,getCharacterEncoding());
//			writer=new PrintWriter(w);
//		}
//		return writer;
//	}
//}
///*
// * Servlet output stream that gobbles up all its data.
// */
//
//// file private
//class NoBodyOutputStream extends ServletOutputStream {
//	private static final String LSTRING_FILE="javax.servlet.http.LocalStrings";
//	private static ResourceBundle lStrings=ResourceBundle.getBundle(LSTRING_FILE);
//	private int contentLength=0;
//	// file private
//	NoBodyOutputStream(){}
//	// file private
//	int getContentLength(){
//		return contentLength;
//	}
//	public void write(int b){
//		contentLength++;
//	}
//	public void write(byte buf[], int offset, int len) throws IOException{
//		if (len>=0){
//			contentLength+=len;
//		} else{
//			// XXX
//			// isn't this really an IllegalArgumentException?
//			String msg=lStrings.getString("err.io.negativelength");
//			throw new IOException(msg);
//		}
//	}
//}
////XXX/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///*
//* Licensed to the Apache Software Foundation (ASF) under one or more
//* contributor license agreements.  See the NOTICE file distributed with
//* this work for additional information regarding copyright ownership.
//* The ASF licenses this file to You under the Apache License, Version 2.0
//* (the "License"); you may not use this file except in compliance with
//* the License.  You may obtain a copy of the License at
//*
//*     http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//
///**
//* Defines an object to assist a servlet in sending a response to the client.
//* The servlet container creates a <code>ServletResponse</code> object and
//* passes it as an argument to the servlet's <code>service</code> method.
//*
//* <p>To send binary data in a MIME body response, use
//* the {@link ServletOutputStream} returned by {@link #getOutputStream}.
//* To send character data, use the <code>PrintWriter</code> object 
//* returned by {@link #getWriter}. To mix binary and text data,
//* for example, to create a multipart response, use a
//* <code>ServletOutputStream</code> and manage the character sections
//* manually.
//*
//* <p>The charset for the MIME body response can be specified
//* explicitly using the {@link #setCharacterEncoding} and
//* {@link #setContentType} methods, or implicitly
//* using the {@link #setLocale} method.
//* Explicit specifications take precedence over
//* implicit specifications. If no charset is specified, ISO-8859-1 will be
//* used. The <code>setCharacterEncoding</code>,
//* <code>setContentType</code>, or <code>setLocale</code> method must
//* be called before <code>getWriter</code> and before committing
//* the response for the character encoding to be used.
//* 
//* <p>See the Internet RFCs such as 
//* <a href="http://www.ietf.org/rfc/rfc2045.txt">
//* RFC 2045</a> for more information on MIME. Protocols such as SMTP
//* and HTTP define profiles of MIME, and those standards
//* are still evolving.
//*
//* @author 	Various
//* @version 	$Version$
//*
//* @see		ServletOutputStream
//*
//*/
//
//interface ServletResponse {
//
//
//
///**
// * Returns the name of the character encoding (MIME charset)
// * used for the body sent in this response.
// * The character encoding may have been specified explicitly
// * using the {@link #setCharacterEncoding} or
// * {@link #setContentType} methods, or implicitly using the
// * {@link #setLocale} method. Explicit specifications take
// * precedence over implicit specifications. Calls made
// * to these methods after <code>getWriter</code> has been
// * called or after the response has been committed have no
// * effect on the character encoding. If no character encoding
// * has been specified, <code>ISO-8859-1</code> is returned.
// * <p>See RFC 2047 (http://www.ietf.org/rfc/rfc2047.txt)
// * for more information about character encoding and MIME.
// *
// * @return		a <code>String</code> specifying the
// *			name of the character encoding, for
// *			example, <code>UTF-8</code>
// *
// */
//
//public String getCharacterEncoding();
//
//
//
///**
// * Returns the content type used for the MIME body
// * sent in this response. The content type proper must
// * have been specified using {@link #setContentType}
// * before the response is committed. If no content type
// * has been specified, this method returns null.
// * If a content type has been specified and a
// * character encoding has been explicitly or implicitly
// * specified as described in {@link #getCharacterEncoding},
// * the charset parameter is included in the string returned.
// * If no character encoding has been specified, the
// * charset parameter is omitted.
// *
// * @return		a <code>String</code> specifying the
// *			content type, for example,
// *			<code>text/html; charset=UTF-8</code>,
// *			or null
// *
// * @since 2.4
// */
//
//public String getContentType();
//
//
//
///**
// * Returns a {@link ServletOutputStream} suitable for writing binary 
// * data in the response. The servlet container does not encode the
// * binary data.  
// 
// * <p> Calling flush() on the ServletOutputStream commits the response.
// 
// * Either this method or {@link #getWriter} may 
// * be called to write the body, not both.
// *
// * @return				a {@link ServletOutputStream} for writing binary data	
// *
// * @exception IllegalStateException if the <code>getWriter</code> method
// * 					has been called on this response
// *
// * @exception IOException 		if an input or output exception occurred
// *
// * @see 				#getWriter
// *
// */
//
//public ServletOutputStream getOutputStream() throws IOException;
//
//
//
///**
// * Returns a <code>PrintWriter</code> object that
// * can send character text to the client.
// * The <code>PrintWriter</code> uses the character
// * encoding returned by {@link #getCharacterEncoding}.
// * If the response's character encoding has not been
// * specified as described in <code>getCharacterEncoding</code>
// * (i.e., the method just returns the default value 
// * <code>ISO-8859-1</code>), <code>getWriter</code>
// * updates it to <code>ISO-8859-1</code>.
// * <p>Calling flush() on the <code>PrintWriter</code>
// * commits the response.
// * <p>Either this method or {@link #getOutputStream} may be called
// * to write the body, not both.
// *
// * 
// * @return 		a <code>PrintWriter</code> object that 
// *			can return character data to the client 
// *
// * @exception UnsupportedEncodingException
// *			if the character encoding returned
// *			by <code>getCharacterEncoding</code> cannot be used
// *
// * @exception IllegalStateException
// *			if the <code>getOutputStream</code>
// * 			method has already been called for this 
// *			response object
// *
// * @exception IOException
// *			if an input or output exception occurred
// *
// * @see 		#getOutputStream
// * @see 		#setCharacterEncoding
// *
// */
//
//public PrintWriter getWriter() throws IOException;
//
//
//
//
///**
// * Sets the character encoding (MIME charset) of the response
// * being sent to the client, for example, to UTF-8.
// * If the character encoding has already been set by
// * {@link #setContentType} or {@link #setLocale},
// * this method overrides it.
// * Calling {@link #setContentType} with the <code>String</code>
// * of <code>text/html</code> and calling
// * this method with the <code>String</code> of <code>UTF-8</code>
// * is equivalent with calling
// * <code>setContentType</code> with the <code>String</code> of
// * <code>text/html; charset=UTF-8</code>.
// * <p>This method can be called repeatedly to change the character
// * encoding.
// * This method has no effect if it is called after
// * <code>getWriter</code> has been
// * called or after the response has been committed.
// * <p>Containers must communicate the character encoding used for
// * the servlet response's writer to the client if the protocol
// * provides a way for doing so. In the case of HTTP, the character
// * encoding is communicated as part of the <code>Content-Type</code>
// * header for text media types. Note that the character encoding
// * cannot be communicated via HTTP headers if the servlet does not
// * specify a content type; however, it is still used to encode text
// * written via the servlet response's writer.
// *
// * @param charset 	a String specifying only the character set
// * 			defined by IANA Character Sets
// *			(http://www.iana.org/assignments/character-sets)
// *
// * @see		#setContentType
// * 			#setLocale
// *
// * @since 2.4
// *
// */
//
//public void setCharacterEncoding(String charset);
//
//
//
//
///**
// * Sets the length of the content body in the response
// * In HTTP servlets, this method sets the HTTP Content-Length header.
// *
// *
// * @param len 	an integer specifying the length of the 
// * 			content being returned to the client; sets
// *			the Content-Length header
// *
// */
//
//public void setContentLength(int len);
//
//
//
///**
// * Sets the content type of the response being sent to
// * the client, if the response has not been committed yet.
// * The given content type may include a character encoding
// * specification, for example, <code>text/html;charset=UTF-8</code>.
// * The response's character encoding is only set from the given
// * content type if this method is called before <code>getWriter</code>
// * is called.
// * <p>This method may be called repeatedly to change content type and
// * character encoding.
// * This method has no effect if called after the response
// * has been committed. It does not set the response's character
// * encoding if it is called after <code>getWriter</code>
// * has been called or after the response has been committed.
// * <p>Containers must communicate the content type and the character
// * encoding used for the servlet response's writer to the client if
// * the protocol provides a way for doing so. In the case of HTTP,
// * the <code>Content-Type</code> header is used.
// *
// * @param type 	a <code>String</code> specifying the MIME 
// *			type of the content
// *
// * @see 		#setLocale
// * @see 		#setCharacterEncoding
// * @see 		#getOutputStream
// * @see 		#getWriter
// *
// */
//
//public void setContentType(String type);
//
//
///**
// * Sets the preferred buffer size for the body of the response.  
// * The servlet container will use a buffer at least as large as 
// * the size requested.  The actual buffer size used can be found
// * using <code>getBufferSize</code>.
// *
// * <p>A larger buffer allows more content to be written before anything is
// * actually sent, thus providing the servlet with more time to set
// * appropriate status codes and headers.  A smaller buffer decreases 
// * server memory load and allows the client to start receiving data more
// * quickly.
// *
// * <p>This method must be called before any response body content is
// * written; if content has been written or the response object has
// * been committed, this method throws an 
// * <code>IllegalStateException</code>.
// *
// * @param size 	the preferred buffer size
// *
// * @exception  IllegalStateException  	if this method is called after
// *						content has been written
// *
// * @see 		#getBufferSize
// * @see 		#flushBuffer
// * @see 		#isCommitted
// * @see 		#reset
// *
// */
//
//public void setBufferSize(int size);
//
//
//
///**
// * Returns the actual buffer size used for the response.  If no buffering
// * is used, this method returns 0.
// *
// * @return	 	the actual buffer size used
// *
// * @see 		#setBufferSize
// * @see 		#flushBuffer
// * @see 		#isCommitted
// * @see 		#reset
// *
// */
//
//public int getBufferSize();
//
//
//
///**
// * Forces any content in the buffer to be written to the client.  A call
// * to this method automatically commits the response, meaning the status 
// * code and headers will be written.
// *
// * @see 		#setBufferSize
// * @see 		#getBufferSize
// * @see 		#isCommitted
// * @see 		#reset
// *
// */
//
//public void flushBuffer() throws IOException;
//
//
//
///**
// * Clears the content of the underlying buffer in the response without
// * clearing headers or status code. If the 
// * response has been committed, this method throws an 
// * <code>IllegalStateException</code>.
// *
// * @see 		#setBufferSize
// * @see 		#getBufferSize
// * @see 		#isCommitted
// * @see 		#reset
// *
// * @since 2.3
// */
//
//public void resetBuffer();
//
//
///**
// * Returns a boolean indicating if the response has been
// * committed.  A committed response has already had its status 
// * code and headers written.
// *
// * @return		a boolean indicating if the response has been
// *  		committed
// *
// * @see 		#setBufferSize
// * @see 		#getBufferSize
// * @see 		#flushBuffer
// * @see 		#reset
// *
// */
//
//public boolean isCommitted();
//
//
//
///**
// * Clears any data that exists in the buffer as well as the status code and
// * headers.  If the response has been committed, this method throws an 
// * <code>IllegalStateException</code>.
// *
// * @exception IllegalStateException  if the response has already been
// *                                   committed
// *
// * @see 		#setBufferSize
// * @see 		#getBufferSize
// * @see 		#flushBuffer
// * @see 		#isCommitted
// *
// */
//
//public void reset();
//
//
//
///**
// * Sets the locale of the response, if the response has not been
// * committed yet. It also sets the response's character encoding
// * appropriately for the locale, if the character encoding has not
// * been explicitly set using {@link #setContentType} or
// * {@link #setCharacterEncoding}, <code>getWriter</code> hasn't
// * been called yet, and the response hasn't been committed yet.
// * If the deployment descriptor contains a 
// * <code>locale-encoding-mapping-list</code> element, and that
// * element provides a mapping for the given locale, that mapping
// * is used. Otherwise, the mapping from locale to character
// * encoding is container dependent.
// * <p>This method may be called repeatedly to change locale and
// * character encoding. The method has no effect if called after the
// * response has been committed. It does not set the response's
// * character encoding if it is called after {@link #setContentType}
// * has been called with a charset specification, after
// * {@link #setCharacterEncoding} has been called, after
// * <code>getWriter</code> has been called, or after the response
// * has been committed.
// * <p>Containers must communicate the locale and the character encoding
// * used for the servlet response's writer to the client if the protocol
// * provides a way for doing so. In the case of HTTP, the locale is
// * communicated via the <code>Content-Language</code> header,
// * the character encoding as part of the <code>Content-Type</code>
// * header for text media types. Note that the character encoding
// * cannot be communicated via HTTP headers if the servlet does not
// * specify a content type; however, it is still used to encode text
// * written via the servlet response's writer.
// * 
// * @param loc  the locale of the response
// *
// * @see 		#getLocale
// * @see 		#setContentType
// * @see 		#setCharacterEncoding
// *
// */
//
//public void setLocale(Locale loc);
//
//
//
///**
// * Returns the locale specified for this response
// * using the {@link #setLocale} method. Calls made to
// * <code>setLocale</code> after the response is committed
// * have no effect. If no locale has been specified,
// * the container's default locale is returned.
// * 
// * @see 		#setLocale
// *
// */
//
//public Locale getLocale();
//
//
//
//}
////XXX//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//interface HttpServletResponse extends ServletResponse {
//
//    /**
//     * Adds the specified cookie to the response.  This method can be called
//     * multiple times to set more than one cookie.
//     *
//     * @param cookie the Cookie to return to the client
//     *
//     */
//
//    public void addCookie(Cookie cookie);
//
//    /**
//     * Returns a boolean indicating whether the named response header 
//     * has already been set.
//     * 
//     * @param	name	the header name
//     * @return		<code>true</code> if the named response header 
//     *			has already been set; 
//     * 			<code>false</code> otherwise
//     */
//
//    public boolean containsHeader(String name);
//
//    /**
//     * Encodes the specified URL by including the session ID in it,
//     * or, if encoding is not needed, returns the URL unchanged.
//     * The implementation of this method includes the logic to
//     * determine whether the session ID needs to be encoded in the URL.
//     * For example, if the browser supports cookies, or session
//     * tracking is turned off, URL encoding is unnecessary.
//     * 
//     * <p>For robust session tracking, all URLs emitted by a servlet 
//     * should be run through this
//     * method.  Otherwise, URL rewriting cannot be used with browsers
//     * which do not support cookies.
//     *
//     * @param	url	the url to be encoded.
//     * @return		the encoded URL if encoding is needed;
//     * 			the unchanged URL otherwise.
//     */
//
//    public String encodeURL(String url);
//
//    /**
//     * Encodes the specified URL for use in the
//     * <code>sendRedirect</code> method or, if encoding is not needed,
//     * returns the URL unchanged.  The implementation of this method
//     * includes the logic to determine whether the session ID
//     * needs to be encoded in the URL.  Because the rules for making
//     * this determination can differ from those used to decide whether to
//     * encode a normal link, this method is separated from the
//     * <code>encodeURL</code> method.
//     * 
//     * <p>All URLs sent to the <code>HttpServletResponse.sendRedirect</code>
//     * method should be run through this method.  Otherwise, URL
//     * rewriting cannot be used with browsers which do not support
//     * cookies.
//     *
//     * @param	url	the url to be encoded.
//     * @return		the encoded URL if encoding is needed;
//     * 			the unchanged URL otherwise.
//     *
//     * @see #sendRedirect
//     * @see #encodeUrl
//     */
//
//    public String encodeRedirectURL(String url);
//
//    /**
//     * @deprecated	As of version 2.1, use encodeURL(String url) instead
//     *
//     * @param	url	the url to be encoded.
//     * @return		the encoded URL if encoding is needed; 
//     * 			the unchanged URL otherwise.
//     */
//
//    public String encodeUrl(String url);
//    
//    /**
//     * @deprecated	As of version 2.1, use 
//     *			encodeRedirectURL(String url) instead
//     *
//     * @param	url	the url to be encoded.
//     * @return		the encoded URL if encoding is needed; 
//     * 			the unchanged URL otherwise.
//     */
//
//    public String encodeRedirectUrl(String url);
//
//    /**
//     * Sends an error response to the client using the specified
//     * status.  The server defaults to creating the
//     * response to look like an HTML-formatted server error page
//     * containing the specified message, setting the content type
//     * to "text/html", leaving cookies and other headers unmodified.
//     *
//     * If an error-page declaration has been made for the web application
//     * corresponding to the status code passed in, it will be served back in 
//     * preference to the suggested msg parameter. 
//     *
//     * <p>If the response has already been committed, this method throws 
//     * an IllegalStateException.
//     * After using this method, the response should be considered
//     * to be committed and should not be written to.
//     *
//     * @param	sc	the error status code
//     * @param	msg	the descriptive message
//     * @exception	IOException	If an input or output exception occurs
//     * @exception	IllegalStateException	If the response was committed
//     */
//   
//    public void sendError(int sc, String msg) throws IOException;
//
//    /**
//     * Sends an error response to the client using the specified status
//     * code and clearing the buffer. 
//     * <p>If the response has already been committed, this method throws 
//     * an IllegalStateException.
//     * After using this method, the response should be considered
//     * to be committed and should not be written to.
//     *
//     * @param	sc	the error status code
//     * @exception	IOException	If an input or output exception occurs
//     * @exception	IllegalStateException	If the response was committed
//     *						before this method call
//     */
//
//    public void sendError(int sc) throws IOException;
//
//    /**
//     * Sends a temporary redirect response to the client using the
//     * specified redirect location URL.  This method can accept relative URLs;
//     * the servlet container must convert the relative URL to an absolute URL
//     * before sending the response to the client. If the location is relative 
//     * without a leading '/' the container interprets it as relative to
//     * the current request URI. If the location is relative with a leading
//     * '/' the container interprets it as relative to the servlet container root.
//     *
//     * <p>If the response has already been committed, this method throws 
//     * an IllegalStateException.
//     * After using this method, the response should be considered
//     * to be committed and should not be written to.
//     *
//     * @param		location	the redirect location URL
//     * @exception	IOException	If an input or output exception occurs
//     * @exception	IllegalStateException	If the response was committed or
// if a partial URL is given and cannot be converted into a valid URL
//     */
//
//    public void sendRedirect(String location) throws IOException;
//    
//    /**
//     * 
//     * Sets a response header with the given name and
//     * date-value.  The date is specified in terms of
//     * milliseconds since the epoch.  If the header had already
//     * been set, the new value overwrites the previous one.  The
//     * <code>containsHeader</code> method can be used to test for the
//     * presence of a header before setting its value.
//     * 
//     * @param	name	the name of the header to set
//     * @param	date	the assigned date value
//     * 
//     * @see #containsHeader
//     * @see #addDateHeader
//     */
//
//    public void setDateHeader(String name, long date);
//    
//    /**
//     * 
//     * Adds a response header with the given name and
//     * date-value.  The date is specified in terms of
//     * milliseconds since the epoch.  This method allows response headers 
//     * to have multiple values.
//     * 
//     * @param	name	the name of the header to set
//     * @param	date	the additional date value
//     * 
//     * @see #setDateHeader
//     */
//
//    public void addDateHeader(String name, long date);
//    
//    /**
//     *
//     * Sets a response header with the given name and value.
//     * If the header had already been set, the new value overwrites the
//     * previous one.  The <code>containsHeader</code> method can be
//     * used to test for the presence of a header before setting its
//     * value.
//     * 
//     * @param	name	the name of the header
//     * @param	value	the header value  If it contains octet string,
//     *		it should be encoded according to RFC 2047
//     *		(http://www.ietf.org/rfc/rfc2047.txt)
//     *
//     * @see #containsHeader
//     * @see #addHeader
//     */
//
//    public void setHeader(String name, String value);
//    
//    /**
//     * Adds a response header with the given name and value.
//     * This method allows response headers to have multiple values.
//     * 
//     * @param	name	the name of the header
//     * @param	value	the additional header value   If it contains
//     *		octet string, it should be encoded
//     *		according to RFC 2047
//     *		(http://www.ietf.org/rfc/rfc2047.txt)
//     *
//     * @see #setHeader
//     */
//
//    public void addHeader(String name, String value);
//
//    /**
//     * Sets a response header with the given name and
//     * integer value.  If the header had already been set, the new value
//     * overwrites the previous one.  The <code>containsHeader</code>
//     * method can be used to test for the presence of a header before
//     * setting its value.
//     *
//     * @param	name	the name of the header
//     * @param	value	the assigned integer value
//     *
//     * @see #containsHeader
//     * @see #addIntHeader
//     */
//
//    public void setIntHeader(String name, int value);
//
//    /**
//     * Adds a response header with the given name and
//     * integer value.  This method allows response headers to have multiple
//     * values.
//     *
//     * @param	name	the name of the header
//     * @param	value	the assigned integer value
//     *
//     * @see #setIntHeader
//     */
//
//    public void addIntHeader(String name, int value);
//
//
//    
//    /**
//     * Sets the status code for this response.  This method is used to
//     * set the return status code when there is no error (for example,
//     * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there
//     * is an error, and the caller wishes to invoke an error-page defined
//     * in the web application, the <code>sendError</code> method should be used
//     * instead.
//     * <p> The container clears the buffer and sets the Location header, preserving
//     * cookies and other headers.
//     *
//     * @param	sc	the status code
//     *
//     * @see #sendError
//     */
//
//    public void setStatus(int sc);
//  
//    /**
//     * @deprecated As of version 2.1, due to ambiguous meaning of the 
//     * message parameter. To set a status code 
//     * use <code>setStatus(int)</code>, to send an error with a description
//     * use <code>sendError(int, String)</code>.
//     *
//     * Sets the status code and message for this response.
//     * 
//     * @param	sc	the status code
//     * @param	sm	the status message
//     */
//
//    public void setStatus(int sc, String sm);
//
//    
//    /*
//     * Server status codes; see RFC 2068.
//     */
//
//    /**
//     * Status code (100) indicating the client can continue.
//     */
//
//    public static final int SC_CONTINUE = 100;
//
//    
//    /**
//     * Status code (101) indicating the server is switching protocols
//     * according to Upgrade header.
//     */
//
//    public static final int SC_SWITCHING_PROTOCOLS = 101;
//
//    /**
//     * Status code (200) indicating the request succeeded normally.
//     */
//
//    public static final int SC_OK = 200;
//
//    /**
//     * Status code (201) indicating the request succeeded and created
//     * a new resource on the server.
//     */
//
//    public static final int SC_CREATED = 201;
//
//    /**
//     * Status code (202) indicating that a request was accepted for
//     * processing, but was not completed.
//     */
//
//    public static final int SC_ACCEPTED = 202;
//
//    /**
//     * Status code (203) indicating that the meta information presented
//     * by the client did not originate from the server.
//     */
//
//    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
//
//    /**
//     * Status code (204) indicating that the request succeeded but that
//     * there was no new information to return.
//     */
//
//    public static final int SC_NO_CONTENT = 204;
//
//    /**
//     * Status code (205) indicating that the agent <em>SHOULD</em> reset
//     * the document view which caused the request to be sent.
//     */
//
//    public static final int SC_RESET_CONTENT = 205;
//
//    /**
//     * Status code (206) indicating that the server has fulfilled
//     * the partial GET request for the resource.
//     */
//
//    public static final int SC_PARTIAL_CONTENT = 206;
//
//    /**
//     * Status code (300) indicating that the requested resource
//     * corresponds to any one of a set of representations, each with
//     * its own specific location.
//     */
//
//    public static final int SC_MULTIPLE_CHOICES = 300;
//
//    /**
//     * Status code (301) indicating that the resource has permanently
//     * moved to a new location, and that future references should use a
//     * new URI with their requests.
//     */
//
//    public static final int SC_MOVED_PERMANENTLY = 301;
//
//    /**
//     * Status code (302) indicating that the resource has temporarily
//     * moved to another location, but that future references should
//     * still use the original URI to access the resource.
//     *
//     * This definition is being retained for backwards compatibility.
//     * SC_FOUND is now the preferred definition.
//     */
//
//    public static final int SC_MOVED_TEMPORARILY = 302;
//
//    /**
//    * Status code (302) indicating that the resource reside
//    * temporarily under a different URI. Since the redirection might
//    * be altered on occasion, the client should continue to use the
//    * Request-URI for future requests.(HTTP/1.1) To represent the
//    * status code (302), it is recommended to use this variable.
//    */
//
//    public static final int SC_FOUND = 302;
//
//    /**
//     * Status code (303) indicating that the response to the request
//     * can be found under a different URI.
//     */
//
//    public static final int SC_SEE_OTHER = 303;
//
//    /**
//     * Status code (304) indicating that a conditional GET operation
//     * found that the resource was available and not modified.
//     */
//
//    public static final int SC_NOT_MODIFIED = 304;
//
//    /**
//     * Status code (305) indicating that the requested resource
//     * <em>MUST</em> be accessed through the proxy given by the
//     * <code><em>Location</em></code> field.
//     */
//
//    public static final int SC_USE_PROXY = 305;
//
//     /**
//     * Status code (307) indicating that the requested resource 
//     * resides temporarily under a different URI. The temporary URI
//     * <em>SHOULD</em> be given by the <code><em>Location</em></code> 
//     * field in the response.
//     */
//
//     public static final int SC_TEMPORARY_REDIRECT = 307;
//
//    /**
//     * Status code (400) indicating the request sent by the client was
//     * syntactically incorrect.
//     */
//
//    public static final int SC_BAD_REQUEST = 400;
//
//    /**
//     * Status code (401) indicating that the request requires HTTP
//     * authentication.
//     */
//
//    public static final int SC_UNAUTHORIZED = 401;
//
//    /**
//     * Status code (402) reserved for future use.
//     */
//
//    public static final int SC_PAYMENT_REQUIRED = 402;
//
//    /**
//     * Status code (403) indicating the server understood the request
//     * but refused to fulfill it.
//     */
//
//    public static final int SC_FORBIDDEN = 403;
//
//    /**
//     * Status code (404) indicating that the requested resource is not
//     * available.
//     */
//
//    public static final int SC_NOT_FOUND = 404;
//
//    /**
//     * Status code (405) indicating that the method specified in the
//     * <code><em>Request-Line</em></code> is not allowed for the resource
//     * identified by the <code><em>Request-URI</em></code>.
//     */
//
//    public static final int SC_METHOD_NOT_ALLOWED = 405;
//
//    /**
//     * Status code (406) indicating that the resource identified by the
//     * request is only capable of generating response entities which have
//     * content characteristics not acceptable according to the accept
//     * headers sent in the request.
//     */
//
//    public static final int SC_NOT_ACCEPTABLE = 406;
//
//    /**
//     * Status code (407) indicating that the client <em>MUST</em> first
//     * authenticate itself with the proxy.
//     */
//
//    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
//
//    /**
//     * Status code (408) indicating that the client did not produce a
//     * request within the time that the server was prepared to wait.
//     */
//
//    public static final int SC_REQUEST_TIMEOUT = 408;
//
//    /**
//     * Status code (409) indicating that the request could not be
//     * completed due to a conflict with the current state of the
//     * resource.
//     */
//
//    public static final int SC_CONFLICT = 409;
//
//    /**
//     * Status code (410) indicating that the resource is no longer
//     * available at the server and no forwarding address is known.
//     * This condition <em>SHOULD</em> be considered permanent.
//     */
//
//    public static final int SC_GONE = 410;
//
//    /**
//     * Status code (411) indicating that the request cannot be handled
//     * without a defined <code><em>Content-Length</em></code>.
//     */
//
//    public static final int SC_LENGTH_REQUIRED = 411;
//
//    /**
//     * Status code (412) indicating that the precondition given in one
//     * or more of the request-header fields evaluated to false when it
//     * was tested on the server.
//     */
//
//    public static final int SC_PRECONDITION_FAILED = 412;
//
//    /**
//     * Status code (413) indicating that the server is refusing to process
//     * the request because the request entity is larger than the server is
//     * willing or able to process.
//     */
//
//    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
//
//    /**
//     * Status code (414) indicating that the server is refusing to service
//     * the request because the <code><em>Request-URI</em></code> is longer
//     * than the server is willing to interpret.
//     */
//
//    public static final int SC_REQUEST_URI_TOO_LONG = 414;
//
//    /**
//     * Status code (415) indicating that the server is refusing to service
//     * the request because the entity of the request is in a format not
//     * supported by the requested resource for the requested method.
//     */
//
//    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
//
//    /**
//     * Status code (416) indicating that the server cannot serve the
//     * requested byte range.
//     */
//
//    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
//
//    /**
//     * Status code (417) indicating that the server could not meet the
//     * expectation given in the Expect request header.
//     */
//
//    public static final int SC_EXPECTATION_FAILED = 417;
//
//    /**
//     * Status code (500) indicating an error inside the HTTP server
//     * which prevented it from fulfilling the request.
//     */
//
//    public static final int SC_INTERNAL_SERVER_ERROR = 500;
//
//    /**
//     * Status code (501) indicating the HTTP server does not support
//     * the functionality needed to fulfill the request.
//     */
//
//    public static final int SC_NOT_IMPLEMENTED = 501;
//
//    /**
//     * Status code (502) indicating that the HTTP server received an
//     * invalid response from a server it consulted when acting as a
//     * proxy or gateway.
//     */
//
//    public static final int SC_BAD_GATEWAY = 502;
//
//    /**
//     * Status code (503) indicating that the HTTP server is
//     * temporarily overloaded, and unable to handle the request.
//     */
//
//    public static final int SC_SERVICE_UNAVAILABLE = 503;
//
//    /**
//     * Status code (504) indicating that the server did not receive
//     * a timely response from the upstream server while acting as
//     * a gateway or proxy.
//     */
//
//    public static final int SC_GATEWAY_TIMEOUT = 504;
//
//    /**
//     * Status code (505) indicating that the server does not support
//     * or refuses to support the HTTP protocol version that was used
//     * in the request message.
//     */
//
//    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
//}