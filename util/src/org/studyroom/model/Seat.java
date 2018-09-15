package org.studyroom.model;

import java.io.*;
import java.util.*;

public class Seat implements Serializable {
	private static final long serialVersionUID=1L;
	private String id;
	private String[] features;
	private boolean cAvailable=true, dAvailable=true;
	public Seat(String id){
		this.id=id;
	}
	public Seat(String id, String...features){
		this(id);
		this.features=features;
	}
	public String getID(){
		return id;
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
		return id;
	}
	@Override
	public boolean equals(Object o){
		return o instanceof Seat && ((Seat)o).getID().equals(id);
	}
	@Override
	public int hashCode(){
		return id.hashCode();
	}
}
