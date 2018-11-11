package org.studyroom.statistics.kp;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

class KPPersistence extends Persistence {
	private final Map<String,StudyRoom> studyRooms;
	private final Map<String,Seat> seats;
	KPPersistence(StudyRoom[] studyRooms, Map<String,Seat> seats){
		this.studyRooms=new HashMap<>();
		for (StudyRoom sr : studyRooms)
			this.studyRooms.put(sr.getID(),sr);
		this.seats=seats;
	}
	KPPersistence(Collection<StudyRoom> studyRooms, Map<String,Seat> seats){
		this.studyRooms=new HashMap<>();
		for (StudyRoom sr : studyRooms)
			this.studyRooms.put(sr.getID(),sr);
		this.seats=seats;
	}
	@Override
	public Collection<StudyRoom> getStudyRooms(){
		return studyRooms.values();
	}
	@Override
	public StudyRoom getStudyRoom(String ID){
		return studyRooms.get(ID);
	}
	@Override
	public Collection<Seat> getSeats(){
		return seats.values();
	}
	public Seat getSeat(String seatID){
		return Optional.ofNullable(seats.get(seatID)).orElseThrow(()->new IllegalArgumentException("Unknown seat ID "+seatID));
	}
	@Override
	public Seat getSeat(String studyRoomID, String seatID){
		return getSeat(seatID);
	}
	void notifyChange(Seat seat, SeatStateChange change){
		notifyObservers(seat,change);
	}
	void initState(Seat seat, SeatStateChange change){
		initObservers(seat,change);
	}
}
