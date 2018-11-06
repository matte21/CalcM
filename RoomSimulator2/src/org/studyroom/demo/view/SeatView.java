package org.studyroom.demo.view;

import javafx.scene.control.*;

public class SeatView extends CheckBox {
	private SeatView(String type){
		getStylesheets().add(getClass().getResource("/res/seat.css").toExternalForm());
		getStyleClass().add(type);
	}
	public static class Chair extends SeatView {
		public Chair(){
			super("chair");
		}
	}
	public static class Table extends SeatView {
		public Table(){
			super("table");
		}
	}
}
