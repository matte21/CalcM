package org.studyroom.model;

import java.io.*;
import java.util.*;

public class Seat implements Serializable {
	private static final long serialVersionUID=1L;
	private String ID;
	private String[] features;
	private boolean cAvailable=true, dAvailable=true;
	public Seat(String URI){
		this.ID=URI;
	}
	public Seat(String URI, String...features){
		this(URI);
		this.features=features;
	}
	public String getID(){
		return ID;
	}
	public String[] getFeatures(){
		return Arrays.copyOf(features,features.length);
	}
	public boolean isDeskAvailable(){
		return dAvailable;
	}
	public void setDeskAvailable(boolean available){
		dAvailable=available;
	}
	public boolean isChairAvailable(){
		return cAvailable;
	}
	public void setChairAvailable(boolean available){
		cAvailable=available;
	}
	public boolean isAvailable(){
		return cAvailable && dAvailable;
	}
	@Override
	public String toString(){
		return ID;
	}
	@Override
	public boolean equals(Object o){
		return o instanceof Seat && ((Seat)o).getID().equals(ID);
	}
	@Override
	public int hashCode(){
		return ID.hashCode();
	}
}
