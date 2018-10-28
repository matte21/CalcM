package org.studyroom.statistics;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import org.studyroom.statistics.kp.*;
import org.studyroom.statistics.statistics.*;
import org.studyroom.statistics.view.fx.*;
import org.studyroom.statistics.view.web.*;
import org.studyroom.statistics.viewmodel.*;
import org.studyroom.web.*;

public class Main {
	public static void main(String[] args){
		//KPStatistics kp=SIBKP.getInstance();
		KPStatistics kp=MockKP.getInstance();
		kp.initPersistence();
		Statistic.loadStatistics();
		kp.start();
		WebApp.setViewModelClass(GraphicViewModel.class);
		WebApp.init();
		if (Thread.currentThread().getStackTrace().length>1){
			WebServer s=WebServer.getInstance();
			if (!s.wasStarted())
				try {
					System.out.println("Starting web server");
					s.start(0,false);	//default:true
				} catch (IOException e){
					e.printStackTrace();
				}
		}
		if (SystemTray.isSupported()){
			App.setViewModelClass(GraphicViewModel.class);
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
