package org.studyroom.model;

import java.io.*;
import java.util.*;

public class Table implements Serializable {
	private static final long serialVersionUID=1L;
	private String ID;
	private Seat[] seats;
	private String[] features;
	public Table(String ID, Seat[] seats){
		this.ID=ID;
		this.seats=seats;
	}
	public Table(String ID, Seat[] seats, String...features){
		this(ID,seats);
		this.features=features;
	}
	public String getID(){
		return ID;
	}
	public String[] getFeatures(){
		return Arrays.copyOf(features,features.length);
	}
	public Seat[] getSeats(){
		return Arrays.copyOf(seats,seats.length);
	}
	public Seat getSeat(String URI){
		for (Seat s : seats)
			if (s.getID().equals(URI))
				return s;
		return null;
	}
	@Override
	public String toString(){
		return ID;
	}
	@Override
	public boolean equals(Object o){
		return o instanceof Table && ((Table)o).getID().equals(ID);
	}
	@Override
	public int hashCode(){
		return ID.hashCode();
	}
}
