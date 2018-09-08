package org.studyroom.statistics.persistence;

public class SeatStateChangedEvent {
	private final String seat,table,studyRoom;
	private final SeatStateChange change,other;
	private final boolean init;
	public SeatStateChangedEvent(String seatID, String tableID, String studyRoomID, SeatStateChange change, SeatStateChange other){
		this(seatID,tableID,studyRoomID,change,other,false);
	}
	public SeatStateChangedEvent(String seatID, String tableID, String studyRoomID, SeatStateChange change, SeatStateChange other, boolean initEvent){
		seat=seatID;
		table=tableID;
		studyRoom=studyRoomID;
		this.change=change;
		this.other=other;
		init=initEvent;
	}
	public String getSeatID(){
		return seat;
	}
	public String getTableID(){
		return table;
	}
	public String getStudyRoomID(){
		return studyRoom;
	}
	public SeatStateChange getChange(){
		return change;
	}
	public boolean isSeatAvailable(){
		return change.isFree() && other.isFree();
	}
	public boolean isSeatPartiallyAvailable(){
		return change.isChairChanged()?change.isFree():other.isFree();
	}
	public boolean hasSeatAvailableChanged(){
		return other.isFree();
	}
	public boolean hasSeatPartiallyAvailableChanged(){
		return change.isChairChanged();
	}
	public boolean isInitEvent(){
		return init;
	}
}
