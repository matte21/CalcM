package org.studyroom.statistics.persistence;

import java.util.*;
import java.util.stream.*;
import org.studyroom.model.*;

public abstract class Persistence extends Observable {
	private static Persistence instance;
	/*public static Persistence create(String type){
		try{
			instance=(Persistence)Class.forName(Persistence.class.getName().replace("Persistence",type)).getDeclaredMethod("getInstance").invoke(null);
			return instance;
		} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|ClassNotFoundException|ClassCastException e){
			throw new IllegalArgumentException("Persistence not found");
		}
	}*/
	private static void setInstance(Persistence p){
		if (instance!=null)
			throw new IllegalStateException("Persistence already created");
		instance=p;
	}
	public static Persistence getInstance(){
		if (instance==null)
			throw new IllegalStateException("Persistence not already initialized");
		return instance;
	}
	protected Persistence(){
		setInstance(this);
	}
	protected void notifyObservers(String seatURI, String tableURI, String studyRoomURI, SeatStateChange change, SeatStateChange other){
		setChanged();
		notifyObservers(new SeatStateChangedEvent(seatURI,tableURI,studyRoomURI,change,other));
	}
	public abstract Collection<StudyRoom> getStudyRooms();
	public Collection<String> getStudyRoomsNames(){
		return getStudyRooms().stream().map(StudyRoom::getName).collect(Collectors.toList());
	}
	public Collection<String> getStudyRoomsURIs(){
		return getStudyRooms().stream().map(StudyRoom::getURI).collect(Collectors.toList());
	}
	public String getStudyRoomName(String URI){
		return getStudyRooms().stream().filter(r->r.getURI().equals(URI)).map(StudyRoom::getName).findFirst().orElseThrow(()->new IllegalArgumentException("URI "+URI+" not found"));
	}
}
