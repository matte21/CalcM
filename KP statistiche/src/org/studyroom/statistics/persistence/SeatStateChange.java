package org.studyroom.statistics.persistence;

public enum SeatStateChange {
	DESK_FREE(false,false),DESK_OCCUPIED(false,true),CHAIR_FREE(true,false),CHAIR_OCCUPIED(true,true);
	private boolean chairChange, occupied;
	SeatStateChange(boolean chairChange, boolean occupied){
		this.chairChange=chairChange;
		this.occupied=occupied;
	}
	public boolean isChairChanged(){
		return chairChange;
	}
	public boolean isDeskChanged(){
		return !chairChange;
	}
	public boolean isOccupied(){
		return occupied;
	}
	public boolean isFree(){
		return !occupied;
	}
	public static SeatStateChange chair(boolean occupied){
		return occupied?CHAIR_OCCUPIED:CHAIR_FREE;
	}
	public static SeatStateChange desk(boolean occupied){
		return occupied?DESK_OCCUPIED:DESK_FREE;
	}
}
