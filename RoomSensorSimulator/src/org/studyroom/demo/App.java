package org.studyroom.demo;

import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import org.studyroom.demo.kp.*;
import org.studyroom.demo.view.*;
import org.studyroom.demo.viewmodel.*;

public class App extends Application {
	public static void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage w) throws Exception {
		String host=getParameters().getNamed().get("host");
		String[] args=getParameters().getUnnamed().toArray(new String[0]);
		if (args.length==0){
			System.err.println("Syntax: App [--host=<SIBHost>] {<studyRoomID>}");
			System.exit(1);
		}
		KPSensorSimulator kp=host==null?new KPSensorSimulator():new KPSensorSimulator(host);
		w.setTitle("Room simulator");
		w.setScene(new Scene(new RoomView(new RoomViewModel(kp,args))));
		w.show();
	}
}
