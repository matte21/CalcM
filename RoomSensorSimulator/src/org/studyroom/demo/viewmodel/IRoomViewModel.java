package org.studyroom.demo.viewmodel;

import java.util.*;
import org.studyroom.viewmodel.*;

public interface IRoomViewModel extends IViewModel {
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
