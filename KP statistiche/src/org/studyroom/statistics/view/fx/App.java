package org.studyroom.statistics.view.fx;

import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import org.studyroom.statistics.viewmodel.*;

public class App extends Application {
	private static Class<? extends IGraphicViewModel> vmClass;
	private static App instance;
	public static void setViewModelClass(Class<? extends IGraphicViewModel> c){
		vmClass=c;
	}
	public static void show(){
		Platform.runLater(()->instance.preapreWindow(new Stage()));
	}
	@Override
	public void start(Stage w){
		Platform.setImplicitExit(false);
		instance=this;
		//preapreWindow(w,false);
	}
	private void preapreWindow(Stage w){
		w.setTitle("StudyRoom - Statistiche");
		try{
			IGraphicViewModel vm=vmClass.newInstance();
			w.setScene(new Scene(new GraphicView(vm)));
			w.setOnHidden(e->{
				vm.unbind();
				w.setScene(null);
				System.gc();
			});
		} catch (ReflectiveOperationException e){}
		w.show();
	}
}
