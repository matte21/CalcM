package query.server.handlers;

import org.studyroom.web.HTMLPage;
import org.studyroom.web.Session;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import query.server.exceptions.SIBConnectionErrorException;
import query.server.utils.SibConnectionFactory;
import query.server.utils.query.PrefixedQueryUtilsFactory;

public abstract class SIBFacingHandler extends HTMLPage {
	
	protected static Session getSession(IHTTPSession request){
		Session session = HTMLPage.getSession(request);
		
		if (!session.containsKey("sibConn")) {
			try {
				session.set("sibConn", SibConnectionFactory.getInstance().getSIBConnection());
			} catch (SIBConnectionErrorException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		if (!session.containsKey("queryUtilsFactory")) {
			session.set("queryUtilsFactory", PrefixedQueryUtilsFactory.getInstance());		
		}
		
		return session;
	}
	

}
