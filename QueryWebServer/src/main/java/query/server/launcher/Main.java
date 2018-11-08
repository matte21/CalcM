package query.server.launcher;

import java.io.*;
import org.apache.logging.log4j.*;
import org.studyroom.web.*;
import query.server.handlers.*;
import query.server.utils.*;

public class Main {

	private static final Logger LOG = LogManager.getLogger();
	
	public static void main(String[] args) throws IOException {
		LOG.info("Initializing connection factory...");
		initSibConnectionFactory(args[0], Integer.parseInt(args[1]), args[2]);	

		WebServer server = WebServer.getInstance();		
		server.addRoute("/", BaseHandler.class);

		LOG.info("Query web server is about to start...");
		if (Thread.currentThread().getStackTrace().length==2){
			WebServer s=WebServer.getInstance();
			if (!s.wasStarted())
				try {
					System.out.println("Starting web server");
					s.start(0,false);	//default:true
				} catch (IOException e){
					e.printStackTrace();
				}
		}
		LOG.info("Query web server is now listening on port 80.");
	}

	private static void initSibConnectionFactory(String sibIPorHost, int sibPort, String smartSpaceName) {
		SibConnectionFactory.init(sibIPorHost, sibPort, smartSpaceName);		
	}
	
}
