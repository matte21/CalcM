package org.studyroom.model;

import java.io.*;
import java.util.*;

public class Seat implements Serializable {
	private static final long serialVersionUID=1L;
	private String URI;
	private String[] features;
	private boolean cAvailable=true, dAvailable=true;
	public Seat(String URI){
		this.URI=URI;
	}
	public Seat(String URI, String...features){
		this(URI);
		this.features=features;
	}
	public String getURI(){
		return URI;
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
		return URI;
	}
}
