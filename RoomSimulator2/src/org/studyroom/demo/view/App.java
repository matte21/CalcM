package org.studyroom.demo.view;

import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import org.studyroom.demo.kp.*;
import org.studyroom.demo.viewmodel.*;
import org.studyroom.view.*;

public class App extends Application {
	public static void main(String[] args){
		launch(args);
		System.exit(0);
	}

	@Override
	public void start(Stage w) throws Exception {
		String host=getParameters().getNamed().get("host");
		String[] args=getParameters().getUnnamed().toArray(new String[0]);
		KPSensorSimulator kp=host==null?new KPSensorSimulator():new KPSensorSimulator(host);
		w.setTitle("Room simulator");
		w.setMinWidth(150);
		RoomViewModel rvm=new RoomViewModel(kp,args);
		Scene room=new Scene(new RoomView(rvm));
		Scene create=new Scene(new CreateView(new CreateViewModel(kp,rvm)));
		BindingUtil.addPropertyListener("view",rvm,n->w.setScene(n.equals("create")?create:room));
		w.show();
	}
}
