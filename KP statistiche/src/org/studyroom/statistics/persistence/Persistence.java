package org.studyroom.statistics.persistence;

import java.lang.reflect.*;
import java.util.*;
import org.studyroom.model.*;

public abstract class Persistence {
	private static Persistence instance;
	public static Persistence create(String type){
		try{
			instance=(Persistence)Class.forName(Persistence.class.getName().replace("Persistence",type)).getDeclaredMethod("getInstance").invoke(null);
			return instance;
		} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|ClassNotFoundException|ClassCastException e){
			throw new IllegalArgumentException("Persistence not found");
		}
	}
	public static Persistence getInstance(){
		if (instance==null)
			throw new IllegalStateException("Persistence not already initialized");
		return instance;
	}
	public abstract Collection<StudyRoom> getStudyRooms();
}
