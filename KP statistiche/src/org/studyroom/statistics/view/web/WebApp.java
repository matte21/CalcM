package org.studyroom.statistics.view.web;

import java.io.*;
import fi.iki.elonen.router.RouterNanoHTTPD.*;
import org.studyroom.statistics.viewmodel.*;
import org.studyroom.web.*;

public class WebApp {
	static final String VIEW_MODEL_KEY="vm";
	private static Class<? extends IMainViewModel> vmClass;
	public static void setViewModelClass(Class<? extends IMainViewModel> c){
		vmClass=c;
	}
	public static IMainViewModel newViewModel(){
		try {
			return vmClass.newInstance();
		} catch (Exception e){
			throw new Error(e);
		}
	}
	public static void init(){
		WebServer s=WebServer.getInstance();
		s.addRoute("/statistics",GraphicPage.class);
		s.addRoute("/statistics/rsc/.*",StaticPageHandler.class,new File(".."+File.separator+"web"+File.separator+"statistics"));
		s.addWebSocketMapping("statistics",GraphicPage.Socket.class);
		System.out.println(new File(".."+File.separator+"web"+File.separator+"statistics").getAbsolutePath());
		s.addSessionFinalizer(ss->((IMainViewModel)ss.get(VIEW_MODEL_KEY)).unbind());
	}
}
