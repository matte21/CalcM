package org.studyroom.statistics.persistence;

public class SeatStateChangedEvent {
	private final String seat,table,studyRoom;
	private final SeatStateChange change,other;
	public SeatStateChangedEvent(String seatURI, String tableURI, String studyRoomURI, SeatStateChange change, SeatStateChange other){
		seat=seatURI;
		table=tableURI;
		studyRoom=studyRoomURI;
		this.change=change;
		this.other=other;
	}
	public String getSeatURI(){
		return seat;
	}
	public String getTableURI(){
		return table;
	}
	public String getStudyRoomURI(){
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
}
