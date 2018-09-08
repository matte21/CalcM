package org.studyroom.statistics;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import fi.iki.elonen.*;
import org.studyroom.statistics.kp.*;
import org.studyroom.statistics.statistics.*;
import org.studyroom.statistics.view.fx.*;
import org.studyroom.statistics.view.web.*;
import org.studyroom.statistics.viewmodel.*;

public class Main {
	public static void main(String[] args){
		KPStatistics kp=RedSIBKP.getInstance();
		kp.initPersistence();
		Statistic.loadStatistics();
		kp.start();
		WebServer.setViewModelClass(MainViewModel.class);
		NanoHTTPD s=WebServer.getInstance();
		try{
			s.start(0,false);	//default:true
		} catch (IOException e){
			e.printStackTrace();
		}
		if (SystemTray.isSupported()){
			App.setViewModelClass(MainViewModel.class);
			new Thread(()->App.launch(App.class)).start();
			TrayIcon ic=new TrayIcon(new BufferedImage(30,30,BufferedImage.TYPE_INT_RGB),"StudyRoom - Visualizza statistiche");
			ic.setImageAutoSize(true);
			ic.addActionListener(e->App.show());
			try{
				SystemTray.getSystemTray().add(ic);
			} catch (AWTException e){}
		}
	}
}
