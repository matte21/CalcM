package org.studyroom.demo.view;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import org.studyroom.demo.viewmodel.*;
import org.studyroom.view.*;

public class CreateView extends BorderPane implements BindingUtil.ListPropertyListener<Integer> {
	private final ICreateViewModel viewModel;
	private final VBox tables;
	//private final Button ok, canc; 
	public CreateView(ICreateViewModel vm){
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
		MenuBar mb=new MenuBar(new Menu("Opzioni",null,aot,not));
		setTop(mb);
		
		GridPane data=new GridPane();
		data.setPadding(new Insets(10));
		TextField id=new TextField();
		TextField name=new TextField();
		BindingUtil.bindString(id.textProperty(),"id",vm);
		BindingUtil.bindString(name.textProperty(),"name",vm);
		addValidation(id,"idValid",()->vm.validateID());
		addValidation(name,"nameValid",()->vm.validateName());
		data.add(new FlowPane(new Label("ID: "),id),0,0);
		data.add(new FlowPane(new Label("Nome: "),name),1,0);
		setCenter(new FlowPane(Orientation.VERTICAL,data));
		
		tables=new VBox();
		tables.setPadding(data.getPadding());
		BindingUtil.addListPropertyListener("tables",vm,this);
		Button add=new Button("+");
		add.setOnAction(e->vm.addTable());
		setCenter(new VBox(data,tables,add));
		
		Button ok=new Button("OK");
		Button canc=new Button("Annulla");
		ok.setOnAction(e->vm.createStudyRoom());
		canc.setOnAction(e->vm.cancel());
		GridPane commands=new GridPane();
		setBottom(commands);
		commands.add(ok,1,0);
		commands.add(canc,0,0);
		commands.setHgap(50);
		commands.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		commands.setPadding(new Insets(20,50,20,50));
	}
	private Background bOk=new Background(new BackgroundFill(Color.WHITE,null,null)),bErr=new Background(new BackgroundFill(Color.RED,null,null));
	private void addValidation(TextField txt, String prop, Runnable validate){
		BindingUtil.addBooleanPropertyListener(prop,viewModel,v->txt.setBackground(v?bOk:bErr));
		txt.focusedProperty().addListener((p,o,n)->{if (!n) validate.run();});
	}
	private GridPane newTable(int seats){
		GridPane p=new GridPane();
		addSeats(p,0,seats);
		Button add=new Button("+");
		add.setOnAction(e->viewModel.addSeat(tables.getChildren().indexOf(p)));
		p.add(add,seats,0,2,1);
		return p;
	}
	void addSeats(GridPane p, int index, int seats){
		for (int i=0;i<seats;i++){
			SeatView c=new SeatView.Chair(), d=new SeatView.Table();
			EventHandler<ActionEvent> h=e->{
				viewModel.removeSeat(tables.getChildren().indexOf(p));
			};
			c.setOnAction(h);
			d.setOnAction(h);
			p.add(c,index+i,1);
			p.add(d,index+i,0);
		}
	}
	@Override
	public void onAdd(int i, Integer newValue){
		i=Math.min(i,tables.getChildren().size());
		if (i<0)
			i=0;
		tables.getChildren().add(i,newTable(newValue));
		resizeWindow();
	}
	@Override
	public void onUpdate(int i, Integer newV){
		if (newV<=0){
			onRemove(i,1);
			return;
		}
		i=Math.min(i,tables.getChildren().size()-1);
		if (i<0)
			i=0;
		GridPane p=(GridPane)tables.getChildren().get(i);
		/*javafx.scene.Node add=p.getChildren().filtered(n->n instanceof Button).get(0);
		p.getChildren().clear();
		p.add(add,newV,0,1,2);
		addSeats(p,0,newV);*/
		int oldV=p.getChildren().size()/2;	//"add" button colon = n° seats
		if (newV>oldV)
			addSeats(p,oldV,newV-oldV);
		else {
			p.getChildren().removeIf(n->!(n instanceof Button));
			addSeats(p,0,newV);
			//p.getChildren().removeIf(n->GridPane.getColumnIndex(n)>=newV && GridPane.getRowSpan(n)!=2);
		}
		GridPane.setColumnIndex(p.getChildren().filtered(n->n instanceof Button/*GridPane.getColumnIndex(n)==oldV*/).get(0),newV);
	}
	@Override
	public void onRemove(int i, Integer oldValue){
		i=Math.min(i,tables.getChildren().size()-1);
		if (i<0)
			i=0;
		tables.getChildren().remove(i);
		resizeWindow();
	}
	private void resizeWindow(){
		try {
			Window w=getScene().getWindow();
			w.sizeToScene();
			w.centerOnScreen();
		} catch (NullPointerException e){}
	}
}
