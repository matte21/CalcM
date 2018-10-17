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
	void notifyChange(Seat seat, SeatStateChange change){
		notifyObservers(seat,change);
	}
	void initState(Seat seat, SeatStateChange change){
		initObservers(seat,change);
	}
}
