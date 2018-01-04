package org.studyroom.sensor;

import java.util.*;

public abstract class Sensor extends Observable {
	private boolean s=false;
	protected void set(){
		s=true;
		notifyObservers();
	}
	protected void reset(){
		s=false;
		notifyObservers();
	}
	protected void toggle(){
		s=!s;
		notifyObservers();
	}
	public boolean isOn(){
		return s;
	}
}
