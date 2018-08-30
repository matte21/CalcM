package org.studyroom.model;

import java.io.*;
import java.util.*;

public class Table implements Serializable {
	private static final long serialVersionUID=1L;
	private String URI;
	private Seat[] seats;
	private String[] features;
	public Table(String URI, Seat[] seats){
		this.URI=URI;
		this.seats=seats;
	}
	public Table(String URI, Seat[] seats, String...features){
		this(URI,seats);
		this.features=features;
	}
	public String getURI(){
		return URI;
	}
	public String[] getFeatures(){
		return Arrays.copyOf(features,features.length);
	}
	public Seat[] getSeats(){
		return Arrays.copyOf(seats,seats.length);
	}
	public Seat getSeat(String URI){
		for (Seat s : seats)
			if (s.getURI().equals(URI))
				return s;
		return null;
	}
	@Override
	public String toString(){
		return URI;
	}
	@Override
	public boolean equals(Object o){
		return o instanceof Table && ((Table)o).getURI().equals(URI);
	}
	@Override
	public int hashCode(){
		return URI.hashCode();
	}
}
