package org.studyroom.statistics.view;

import java.util.stream.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import org.studyroom.statistics.viewmodel.*;

public class MainView extends BorderPane {
	private MainViewModel viewModel;
	public MainView(MainViewModel vm){
		viewModel=vm;
		
		MenuBar mb=new MenuBar(
				new Menu("Statistica",null,StatisticViewModel.getStatistics().stream().map(s->{
					MenuItem m=new MenuItem(s);
					m.setOnAction(e->this.viewModel.selectStatistic(s));
					return m;
				}).toArray(MenuItem[]::new)),
				new Menu("Aula",null,vm.getUniversities().stream().map(u->new Menu(u,null,Stream.concat(Stream.of(MainViewModel.DEFAULT_SR),vm.getStudyRooms(u).stream()).map(s->{
					MenuItem m=new MenuItem(s);
					m.setOnAction(e->this.viewModel.selectStudyRoom(s,u));
					return m;
				}).toArray(MenuItem[]::new))).toArray(Menu[]::new))
		);
		setTop(mb);
		Label tit=new Label();
		BindingUtil.bindStringOneWay(tit.textProperty(),"graphicTitle",vm);
		tit.setFont(Font.font("sans-serif",FontWeight.BOLD,24));
		
		Histogram h=Histogram.create(vm.getStatisticViewModel(),"data","tilesLabel");
		BindingUtil.bindObjectOneWay(h.viewModelProperty(),"statisticViewModel",vm);
		
		VBox p=new VBox(tit,h);
		p.setAlignment(Pos.CENTER);
		setCenter(p);
		//tit.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));
	}
}
