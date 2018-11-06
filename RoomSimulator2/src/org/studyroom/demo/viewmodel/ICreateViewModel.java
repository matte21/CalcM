package org.studyroom.demo.viewmodel;

import java.beans.*;
import java.util.*;

public interface ICreateViewModel {
	//for generic view-model
	void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);
	
	//room (only these are properties)
	String getId();
	void setId(String id);
	
	String getName();
	void setName(String name);
	
	String getUniversity();
	void setUniversity(String university);
	
	boolean isIdValid();
	
	boolean isNameValid();
	
	List<Integer> getTables();
	
	//actions
	boolean validateID();
	boolean validateName();
	void addTable();
	void addSeat(int tableIndex);
	void removeSeat(int tableIndex);
	void createStudyRoom();
	void cancel();
}
