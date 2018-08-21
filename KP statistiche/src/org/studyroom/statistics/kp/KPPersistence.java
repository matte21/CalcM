package org.studyroom.statistics.kp;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

class KPPersistence extends Persistence {
	private final StudyRoom[] studyRooms;
	public KPPersistence(StudyRoom[] studyRooms){
		this.studyRooms=studyRooms;
	}
	@Override
	public Collection<StudyRoom> getStudyRooms(){
		return Arrays.asList(studyRooms);
	}
	void notifyChange(String seatURI, String tableURI, String studyRoomURI, SeatStateChange change, SeatStateChange other){
		notifyObservers(seatURI,tableURI,studyRoomURI,change,other);
	}
}
