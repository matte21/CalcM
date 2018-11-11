package org.studyroom.demo.viewmodel;

import java.util.*;
import org.studyroom.viewmodel.*;

public interface ICreateViewModel extends IViewModel {
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
