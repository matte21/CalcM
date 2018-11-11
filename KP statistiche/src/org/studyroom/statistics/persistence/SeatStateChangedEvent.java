package org.studyroom.statistics.persistence;

import org.studyroom.model.*;

public class SeatStateChangedEvent {
	private final Seat seat;
	private final SeatStateChange change;
	private final boolean init;
	public SeatStateChangedEvent(Seat seat, SeatStateChange change){
		this(seat,change,false);
	}
	public SeatStateChangedEvent(Seat seat, SeatStateChange change, boolean initEvent){
		this.seat=seat;
		this.change=change;
		init=initEvent;
	}
	public Seat getSeat(){
		return seat;
	}
	public String getSeatID(){
		return seat.getID();
	}
	public String getTableID(){
		return seat.getTable().getID();
	}
	public String getStudyRoomID(){
		return seat.getTable().getStudyRoom().getID();
	}
	public SeatStateChange getChange(){
		return change;
	}
	public boolean isSeatAvailable(){
		return seat.isAvailable();
	}
	public boolean isSeatPartiallyAvailable(){
		return seat.isChairAvailable();
	}
	public boolean wasSeatAvailable(){
		return (change.isChairChanged() && seat.isDeskAvailable() && !seat.isChairAvailable())||(change.isDeskChanged() && seat.isChairAvailable() && !seat.isDeskAvailable());
	}
	public boolean wasSeatPartiallyAvailable(){
		return change.isChairChanged() ^ seat.isChairAvailable();
	}
	public boolean hasSeatAvailableChanged(){
		return (change.isChairChanged() && seat.isDeskAvailable())||(change.isDeskChanged() && seat.isChairAvailable());
	}
	public boolean hasSeatPartiallyAvailableChanged(){
		return change.isChairChanged();
	}
	public boolean isInitEvent(){
		return init;
	}
}
