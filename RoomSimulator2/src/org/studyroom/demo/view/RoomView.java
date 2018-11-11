package org.studyroom.demo.view;

import java.util.function.*;
import java.util.stream.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import org.studyroom.demo.viewmodel.*;
import org.studyroom.view.*;

public class RoomView extends BorderPane {
	private final IRoomViewModel viewModel;
	private final VBox room;
	public RoomView(IRoomViewModel vm){
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
		MenuItem create=new MenuItem("Nuova...");
		create.setOnAction(e->vm.createRoom());
		Function<String,MenuItem> createRoomMenu=u->new Menu(u,null,vm.getStudyRooms(u).stream().map(s->{
			MenuItem m=new MenuItem(s);
			m.setOnAction(e->this.viewModel.selectStudyRoom(s,u));
			return m;
		}).toArray(MenuItem[]::new));
		Menu rooms=new Menu("Aula",null,vm.getUniversities().stream().map(createRoomMenu).toArray(Menu[]::new));
		BindingUtil.addPropertyListener("studyRooms",vm,l->{
			rooms.getItems().clear();
			rooms.getItems().addAll(vm.getUniversities().stream().map(createRoomMenu).collect(Collectors.toList()));
		});
		MenuBar mb=new MenuBar(rooms,new Menu("Opzioni",null,aot,not,new SeparatorMenuItem(),create));
		setTop(mb);
		Label name=new Label();
		BindingUtil.bindStringOneWay(name.textProperty(),"selectedRoomName",vm);
		name.setFont(Font.font("sans-serif",FontWeight.BOLD,24));
		
		//XXX for semplicity, in the demo we consider only rectangular tables with seats on one side only
		room=new VBox(10);
		room.setPadding(new Insets(20));
		room.setAlignment(Pos.CENTER);
		BindingUtil.addPropertyListener("tables",vm,v->updateRoom());
		
		VBox p=new VBox(name,room);
		p.setAlignment(Pos.CENTER);
		setCenter(p);

	}
	private void updateRoom(){
		room.getChildren().clear();
		room.getChildren().addAll(viewModel.getTables().stream().map(t->{
			GridPane p=new GridPane();
			int i=0;
			for (String[] s : t){
				SeatView c=new SeatView.Chair(), d=new SeatView.Table();
				BindingUtil.bindBoolean(c.selectedProperty(),"state",viewModel,s[0]);
				BindingUtil.bindBoolean(d.selectedProperty(),"state",viewModel,s[1]);
				p.add(c,i,1);
				p.add(d,i,0);
				i++;
			}
			return p;
		}).toArray(GridPane[]::new));
		try {
			Window w=getScene().getWindow();
			w.sizeToScene();
			w.centerOnScreen();
		} catch (NullPointerException e){}
	}
}
