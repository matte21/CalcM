package org.studyroom.statistics.view.fx;

import java.util.stream.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import org.studyroom.statistics.viewmodel.*;

public class MainView extends BorderPane {
	private IMainViewModel viewModel;
	public MainView(IMainViewModel vm){
		viewModel=vm;
		MenuItem aot=new MenuItem("Sempre in primo piano");
		MenuItem not=new MenuItem("Sblocca");
		aot.setOnAction(e->{
			((Stage)getScene().getWindow()).setAlwaysOnTop(true);
			aot.setDisable(true);
			not.setDisable(false);
		});
		not.setOnAction(e->{
			((Stage)getScene().getWindow()).setAlwaysOnTop(false);
			not.setDisable(true);
			aot.setDisable(false);
		});
		not.setDisable(true);
		MenuBar mb=new MenuBar(
				new Menu("Statistica",null,vm.getStatistics().stream().map(s->{
					MenuItem m=new MenuItem(s);
					m.setOnAction(e->this.viewModel.selectStatistic(s));
					return m;
				}).toArray(MenuItem[]::new)),
				new Menu("Aula",null,vm.getUniversities().stream().map(u->new Menu(u,null,Stream.concat(Stream.of(IMainViewModel.DEFAULT_SR),vm.getStudyRooms(u).stream()).map(s->{
					MenuItem m=new MenuItem(s);
					m.setOnAction(e->this.viewModel.selectStudyRoom(s,u));
					return m;
				}).toArray(MenuItem[]::new))).toArray(Menu[]::new)),
				new Menu("Opzioni",null,aot,not)
		);
		setTop(mb);
		Label tit=new Label();
		BindingUtil.bindStringOneWay(tit.textProperty(),"graphicTitle",vm);
		tit.setFont(Font.font("sans-serif",FontWeight.BOLD,24));
		
		Histogram h=Histogram.create(vm,"data","categories","tilesLabel");
		
		VBox p=new VBox(tit,h);
		p.setAlignment(Pos.CENTER);
		setCenter(p);
		//tit.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));
	}
}
