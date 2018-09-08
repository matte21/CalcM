package org.studyroom.statistics.kp;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

class KPPersistence extends Persistence {
	private final StudyRoom[] studyRooms;
	KPPersistence(StudyRoom[] studyRooms){
		this.studyRooms=studyRooms;
	}
	@Override
	public Collection<StudyRoom> getStudyRooms(){
		return Arrays.asList(studyRooms);
	}
	void notifyChange(String seatID, String tableID, String studyRoomID, SeatStateChange change, SeatStateChange other){
		notifyObservers(seatID,tableID,studyRoomID,change,other);
	}
	void initState(String seatID, String tableID, String studyRoomID, SeatStateChange change, SeatStateChange other){
		initObservers(seatID,tableID,studyRoomID,change,other);
	}
}
