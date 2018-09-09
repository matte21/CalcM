package org.studyroom.statistics.view.web;

import java.beans.*;
import java.util.*;
import fi.iki.elonen.NanoHTTPD.*;
import fi.iki.elonen.NanoWSD.*;
import fi.iki.elonen.NanoWSD.WebSocketFrame.*;
import fi.iki.elonen.router.RouterNanoHTTPD.*;
import org.studyroom.statistics.viewmodel.*;
import org.studyroom.web.*;

public class GraphicPage extends HTMLPage {
	@Override
	public Response get(UriResource uriResource, Map<String,String> urlParams, IHTTPSession request){
		StringBuilder html=new StringBuilder();
		Session s=getSession(request);
		IMainViewModel vm=(IMainViewModel)s.getOrSetDefault(WebApp.VIEW_MODEL_KEY,WebApp::newViewModel);
		html.append(
				"<!DOCTYPE html>\n"+
				"<html>\n"+
				"	<head>\n"+
				"		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"+
				"		<title>StudyRoom - statistiche</title>\n"+
				"		<link rel=\"stylesheet\" type=\"text/css\" href=\"statistics/rsc/style.css\">\n"+
				"		<script type=\"text/javascript\" src=\"statistics/rsc/script.js\"></script>\n"+
				"	</head>\n"+
				"	<body>\n"+
				"		<ul id=\"menu\">\n"+
				"			<li class=\"submenu\">\n"+
				"				<div>Statistiche</div>\n"+
				"				<ul>\n");
		for (String st : vm.getStatistics())
			html.append("					<li><a href=\"javascript:selectStatistic('"+st.replace("'","\\'")+"')\">"+st+"</a></li>\n");
		html.append(
				"				</ul>\n"+
				"			</li>\n"+
				"			<li class=\"midmenu\">\n"+
				"				<div>Aule studio</div>\n"+
				"				<ul>\n");
		for (String u : vm.getUniversities()){
			html.append(
					"					<li class=\"submenu\">"+
					"						<div>"+u+"</div>\n"+
					"						<ul>\n"+
					"							<li><a href=\"javascript:selectStudyRoom('"+IMainViewModel.DEFAULT_SR+";university="+u.replace("'","\\'")+"')\">"+IMainViewModel.DEFAULT_SR+"</a></li>\n");
			for (String sr : vm.getStudyRooms(u))
				html.append("							<li><a href=\"javascript:selectStudyRoom('"+sr.replace("'","\\'")+";university="+u.replace("'","\\'")+"')\">"+sr+"</a></li>\n");
			html.append(
					"						</ul>\n"+
					"					</li>\n");
		}
		html.append(
				"				</ul>\n"+
				"			</li>\n"+
				"			<li class=\"submenu\">\n"+
				"				<div>Visualizzazione</div>\n"+
				"				<ul>\n");
		for (String v : vm.getVisualizations())
			html.append("					<li><a href=\"javascript:selectVisualization('"+v+"')\">"+v+"</a></li>\n");
		html.append(
				"				</ul>\n"+
				"			</li>\n"+
				"		</ul>\n"+
				"		<div id=\"cont\">\n");
		html.append(getGraphic(vm));
		html.append(
				"		</div>\n"+
				"	</body>\n"+
				"</html>");
		return getHTMLResponse(html.toString());
	}
//	private IMainViewModel getViewModel(IHTTPSession request){
//		Map<String,Object> s=getSession(request);
//		if (!s.containsKey(VIEW_MODEL))
//			s.put(VIEW_MODEL,WebServer.newViewModel());
//		return (IMainViewModel)s.get(VIEW_MODEL);
//	}
	private static String getGraphic(IMainViewModel vm){
		final int W=1000,H=600;
		StringBuilder html=new StringBuilder();
		html.append(
				"		<h1>"+vm.getGraphicTitle()+"</h1>\n"+
				"		<svg width=\""+W+"\" height=\""+H+"\">\n"+
				"			<rect class=\"background\" x=\"0\" y=\"0\" width=\""+W+"\" height=\""+H+"\"></rect>\n"+
				"			<rect class=\"axis\" x=\"100\" y=\"40\" width=\"1\" height=\""+(H-90)+"\"></rect>\n"+
				"			<rect class=\"axis\" x=\"100\" y=\""+(H-50)+"\" width=\""+(W-190)+"\" height=\"1\"></rect>\n"+
				"			<text class=\"num\" x=\"86\" y=\""+(H-44.6)+"\">0</text>\n");
		Map<String,List<Double>> data=vm.getData();
		double max=Math.max(data.values().stream().mapToDouble(l->l.stream().mapToDouble(Double::doubleValue).sum()).max().orElse(1),1);
		double scaleY=(H-100)/max;
		long og=Math.max((long)Math.log10(max),0);
		og=(long)Math.pow(10,og);
		double mn=max/og;
		long unit;
		final int MAX_STEP=9;
		if (mn<MAX_STEP/5.0)
			unit=og/5;
		else if (mn<MAX_STEP/2.0)
			unit=og/2;
		else if (mn<MAX_STEP)
			unit=og;
		else
			unit=og*2;
		unit=Math.max(unit,1);
		for (long i=unit;i*scaleY<=H-100;i+=unit){
			double h=H-50-i*scaleY;
			html.append(
					"			<rect class=\"step\" x=\"100\" y=\""+h+"\" width=\""+(W-200)+"\" height=\"1\"></rect>\n"+
					"			<text class=\"num\" x=\"86\" y=\""+(h+5.4)+"\">"+i+"</text>\n");
		}
		double scaleX=(W-200)/Math.max(data.size(),1);
		int i=0;
		for (Map.Entry<String,List<Double>> c : data.entrySet()){
			html.append("			<text class=\"cat\" x=\""+(100+scaleX*(i+0.5))+"\" y=\""+(H-28)+"\">"+c.getKey()+"</text>\n");
			double h,hb=H-50;
			for (int j=0;j<c.getValue().size();j++){
				h=c.getValue().get(j)*scaleY;
				html.append("			<rect class=\"graph"+(j+1)+"\" x=\""+(100+scaleX*(i+0.1))+"\" y=\""+(hb-h)+"\" width=\""+(scaleX*0.8)+"\" height=\""+h+"\"></rect>\n");
				hb-=h;
			}
			i++;
		}
		html.append(
				"			<text class=\"cat\" x=\"50\" y=\""+H/2+"\" transform=\"rotate(-90 50,"+H/2+")\">"+vm.getTilesLabel()+"</text>\n"+
				"		</svg>\n"+
				"		<table id=\"legenda\">\n");
		for (i=0;i<vm.getLegend().size();i++)
			html.append(
					"			<tr>\n"+
					"				<td><div class=\"graph"+(i+1)+"\">&nbsp; &nbsp; &nbsp; </div></td>\n"+
					"				<td>"+vm.getLegend().get(i)+"</td>\n"+
					"			</tr>\n");
		html.append("		</table>");
		return html.toString();
	}
	public static class Socket extends WebSocketHandler {
		private Session s;
		private IMainViewModel vm;
		private PropertyChangeListener l=e->update();
		private void update(){
			send(getGraphic(vm));
		}
		@Override
		protected void onOpen(){
			s=getSession();
			vm=(IMainViewModel)s.getOrSetDefault(WebApp.VIEW_MODEL_KEY,WebApp::newViewModel);
			vm.addPropertyChangeListener(l);
		}
		@Override
		protected void onClose(CloseCode code, String reason, boolean initiatedByRemote){
			vm.removePropertyChangeListener(l);
		}
		@Override
		protected void onMessage(WebSocketFrame message){
			s.access();
			String[] m=message.getTextPayload().split("=",2);
			if (m.length<2){
				System.err.println("Malformed command: "+message.getTextPayload());
				return;
			}
			switch (m[0]){
			case "statistic":
				try {
					vm.selectStatistic(m[1]);
				} catch (IllegalArgumentException e){
					System.err.println(m[0]+": "+e.getMessage());
					return;
				}
				break;
			case "visualization":
				try {
					vm.selectVisualization(m[1]);
				} catch (IllegalArgumentException e){
					System.err.println(m[0]+": "+e.getMessage());
					return;
				}
				break;
			case "studyRoom":
				int i=m[1].indexOf(";university=");
				if (i<=0){
					System.err.println("Malformed command: "+message.getTextPayload());
					return;
				}
				String sr=m[1].substring(0,i),u=m[1].substring(i+12);
				try {
					vm.selectStudyRoom(sr,u);
				} catch (IllegalArgumentException e){
					System.err.println(m[0]+": "+e.getMessage());
					return;
				}
				break;
			default:
				System.err.println("Unknown command: "+message.getTextPayload());
				return;
			}
			update();
		}
	}
}
