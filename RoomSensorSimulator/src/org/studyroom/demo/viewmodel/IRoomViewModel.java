package org.studyroom.demo.viewmodel;

import java.beans.*;
import java.util.*;

public interface IRoomViewModel {
	//for generic view-model
	void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);
	
	//room (only these are properties)
	String getSelectedRoomName();
	
	/**@return a collection of lists which represent seats; 
	 * each seat is a String array containing the chair sensor ID and the table sensor ID*/
	Collection<List<String[]>> getTables();
	boolean getState(String sensorID);
	void setState(String sensorID, boolean state);
	
	//menus
	Collection<String> getStudyRooms(String university);
	Collection<String> getUniversities();
	
	//actions
	void selectStudyRoom(String name, String university);
}
