package org.studyroom.sensor;

import java.util.*;

public abstract class Sensor extends Observable {
	private boolean s=false;
	protected void set(){
		s=true;
		setChanged();
		notifyObservers();
	}
	protected void reset(){
		s=false;
		setChanged();
		notifyObservers();
	}
	protected void toggle(){
		s=!s;
		setChanged();
		notifyObservers();
	}
	public boolean isOn(){
		return s;
	}
}
