package org.studyroom.statistics;

import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import org.studyroom.statistics.persistence.*;
import org.studyroom.statistics.view.*;
import org.studyroom.statistics.viewmodel.*;

public class App extends Application {
	@Override
	public void start(Stage w) throws Exception {
		w.setTitle("StudyRoom - Statistiche");
		w.setScene(new Scene(new MainView(new MainViewModel())));
		w.show();
	}
	public static void main(String[] args){
		Persistence.create("MockKP");
		launch();
	}
}
