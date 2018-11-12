package org.studyroom.statistics.view.web;

import java.io.*;
import fi.iki.elonen.router.RouterNanoHTTPD.*;
import org.studyroom.statistics.viewmodel.*;
import org.studyroom.web.*;

public class WebApp {
	static final String VIEW_MODEL_KEY="vm";
	private static Class<? extends IGraphicViewModel> vmClass;
	public static void setViewModelClass(Class<? extends IGraphicViewModel> c){
		vmClass=c;
	}
	public static IGraphicViewModel newViewModel(){
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
		s.addSessionFinalizer(ss->((IGraphicViewModel)ss.get(VIEW_MODEL_KEY)).unbind());
	}
}
