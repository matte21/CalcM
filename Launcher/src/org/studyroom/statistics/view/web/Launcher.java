package org.studyroom.statistics.view.web;

import java.io.*;
import fi.iki.elonen.*;
import org.studyroom.web.*;

public class Launcher {
	public static void main(String[] args){
		NanoHTTPD s=WebServer.getInstance();
		org.studyroom.statistics.Main.main(args);
		//TODO call main of the other apps
		
		if (!s.wasStarted())
			try {
				s.start(0,false);	//default:true
			} catch (IOException e){
				e.printStackTrace();
			}
	}
}
