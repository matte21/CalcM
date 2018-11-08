package org.studyroom.statistics.view.web;

import java.io.*;
import fi.iki.elonen.*;
import org.studyroom.kp.*;
import org.studyroom.web.*;

public class Launcher {
	public static void main(String[] args) throws IOException {
		NanoHTTPD s=WebServer.getInstance();
		org.studyroom.statistics.Main.main(new String[0]);
		query.server.launcher.Main.main(new String[]{"localhost","10010",SIBUtils.SMART_SPACE_NAME});
		//TODO call main of the other apps
		
		if (!s.wasStarted())
			s.start(0,false);	//default:true
	}
}
