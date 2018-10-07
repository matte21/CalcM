package query.server.launcher;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.studyroom.web.WebServer;

import query.server.handlers.BaseHandler;
import query.server.utils.SibConnectionFactory;

public class Main {

	private static final Logger LOG = LogManager.getLogger();
	
	public static void main(String[] args) throws IOException {
		LOG.info("Initializing connection factory...");
		initSibConnectionFactory(args[0], Integer.parseInt(args[1]), args[2]);	

		WebServer server = WebServer.getInstance();		
		server.addRoute("/", BaseHandler.class);

		LOG.info("Query web server is about to start...");
		// I don't know why but with the second argument set to true (AKA daemon mode on) it won't start
		server.start(0, false);
		LOG.info("Query web server is now listening on port 80.");
	}

	private static void initSibConnectionFactory(String sibIPorHost, int sibPort, String smartSpaceName) {
		SibConnectionFactory.init(sibIPorHost, sibPort, smartSpaceName);		
	}
	
}
